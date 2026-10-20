"""Audit only explicit build outputs for the addon SDK; never starts a game."""
from pathlib import Path
import hashlib,json,re,struct,tomllib,zipfile
ROOT=Path(__file__).resolve().parents[1]
VERSION=re.search(r'^mod_version=(.+)$',(ROOT/'gradle.properties').read_text(),re.M)[1].strip()
checks=0
report=[]
def check(ok,label):
 global checks
 checks+=1
 if not ok:raise AssertionError(label)
def refs(data):
 count=struct.unpack_from('>H',data,8)[0];p=10;i=1;utf={};classes=[]
 while i<count:
  tag=data[p];p+=1
  if tag==1:
   n=struct.unpack_from('>H',data,p)[0];p+=2;utf[i]=data[p:p+n].decode('utf-8',errors='replace');p+=n
  elif tag in (7,):classes.append(struct.unpack_from('>H',data,p)[0]);p+=2
  elif tag in (8,16,19,20):p+=2
  elif tag in (3,4,9,10,11,12,17,18):p+=4
  elif tag in (5,6):p+=8;i+=1
  elif tag==15:p+=3
  else:raise AssertionError('Unknown constant-pool tag '+str(tag))
  i+=1
 found={utf[n] for n in classes}
 for value in utf.values():found.update(re.findall(r'L(thaumcraft/[^;<]+)',value))
 return found
jars={
 'api':ROOT/f'api/build/libs/thaumcraft-api-{VERSION}.jar',
 'mod':ROOT/f'mod/build/libs/thaumcraft-1.21.1-{VERSION}.jar',
 'example-addon':ROOT/f'example-addon/build/libs/thaumcraft-example-{VERSION}.jar'}
api_entries={}
with zipfile.ZipFile(jars['api']) as z:api_entries={n:z.read(n) for n in z.namelist() if n.endswith('.class')}
for module,path in jars.items():
 with zipfile.ZipFile(path) as z:
  names=z.namelist();check(len(names)==len(set(names)),module+' duplicate entries')
  check(not any(n.endswith('.jar') for n in names),module+' nested dependency JAR')
  classes=[n for n in names if n.endswith('.class')]
  for name in classes:
   targets=refs(z.read(name))
   if module=='mod':check(not any(t.startswith(('mezz/jei/','dev/emi/emi/')) for t in targets),'core must not link optional viewers')
   if module in ('api','emi-jei-integration-aoptc4','example-addon'):
    allowed=('thaumcraft/api/',)+(('thaumcraft/viewers/',) if module=='emi-jei-integration-aoptc4' else ())
    check(not any(t.startswith('thaumcraft/') and not t.startswith(allowed) for t in targets),module+' imports core internals in '+name)
   if module in ('emi-jei-integration-aoptc4','example-addon'):
    check(not name.startswith(('thaumcraft/api/','mezz/','dev/emi/','net/minecraft/')),module+' shaded API/viewer/game class')
  for directory in [ROOT/module/'build/classes/java/main',ROOT/module/'build/resources/main']:
   if not directory.exists():continue
   for file in directory.rglob('*'):
    if file.is_file():
     name=file.relative_to(directory).as_posix();check(name in names,module+' missing output '+name);check(z.read(name)==file.read_bytes(),module+' stale output '+name)
  check(not any('AddonPersistenceTest' in n or 'IsolationTest' in n or 'RulesTest' in n for n in names),module+' packaged verification class')
  if module=='mod':
   metadata=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode())
   check(metadata['mods'][0]['version']==VERSION,'core metadata version')
   check(not any(d['modId'] in ('thaumcraft_viewers','jei','emi') for d in metadata['dependencies']['thaumcraft']),'core has no viewer dependency declarations')
   check(not any(n.startswith(('thaumcraft/viewers/','mezz/jei/','dev/emi/','thaumcraft/verification/')) for n in names),'core contains no integration or verification classes')
   for name,data in api_entries.items():check(z.read(name)==data,'core missing/stale bundled API '+name)
  if module in ('emi-jei-integration-aoptc4','example-addon'):
   meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode());check(meta['mods'][0]['version']==VERSION,module+' metadata version');deps=meta['dependencies'][meta['mods'][0]['modId']];check(any(d['modId']=='thaumcraft' and d['type']=='required' for d in deps),module+' core dependency')
  for name in names:
   if name.endswith('.json'):json.loads(z.read(name))
  report.append({'artifact':path.name,'bytes':path.stat().st_size,'classes':len(classes),'sha256':hashlib.sha256(path.read_bytes()).hexdigest()})
# Check example links against the builtin research catalog and explicit addon registrations.
builtin={json.loads(p.read_text(encoding='utf-8'))['key'] for p in (ROOT/'mod/src/main/resources/data/thaumcraft/thaumcraft/research').glob('*.json')}
check('THAUMONOMICON' in builtin,'example parent exists')
with zipfile.ZipFile(jars['mod']) as z:check('assets/thaumcraft/textures/foci/frugal.png' in z.namelist(),'example upgrade icon exists in its dependency')
sdk=ROOT/f'build/distributions/thaumcraft-addon-sdk-{VERSION}.zip'
with zipfile.ZipFile(sdk) as z:
 check(set(['docs/README.en.md','docs/README.ru.md','docs/CHECKLIST.en.md','docs/CHECKLIST.ru.md','example-addon/build.gradle',f'lib/thaumcraft-api-{VERSION}.jar',f'lib/thaumcraft-api-{VERSION}-sources.jar']).issubset(z.namelist()),'SDK missing docs/example/API')
 check(not any(n.startswith(('mezz/','dev/emi/')) or 'test-runtime.jar' in n for n in z.namelist()),'SDK includes viewer binaries')
 check(z.read(f'lib/thaumcraft-api-{VERSION}.jar')==jars['api'].read_bytes(),'SDK stale API')
report.append({'artifact':sdk.name,'bytes':sdk.stat().st_size,'sha256':hashlib.sha256(sdk.read_bytes()).hexdigest()})
out=ROOT/'build/reports/addon-artifact-audit.json';out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps({'checks':checks,'artifacts':report},indent=2)+'\n',encoding='utf-8')
print('TC4_ADDON_ARTIFACT_AUDIT_PASS checks='+str(checks))
for item in report:print(item['artifact'],item['bytes'],item['sha256'])
