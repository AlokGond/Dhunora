#!/usr/bin/env python3
"""Release-code launch/navigation smoke check; saves evidence without account credentials."""
import pathlib, re, subprocess, time, xml.etree.ElementTree as ET
out=pathlib.Path('smoke-evidence');out.mkdir(exist_ok=True)
pkg='com.alok.dhunora'
def adb(*args,check=True):
 return subprocess.run(['adb',*args],capture_output=True,check=check).stdout

def capture(label):
 adb('shell','uiautomator','dump','/sdcard/dhunora.xml',check=False)
 xml=adb('shell','cat','/sdcard/dhunora.xml',check=False)
 (out/(label+'.xml')).write_bytes(xml)
 (out/(label+'.png')).write_bytes(adb('exec-out','screencap','-p'))
 return xml

def click_label(xml,label):
 try: nodes=ET.fromstring(xml).iter('node')
 except ET.ParseError: return False
 for n in nodes:
  if n.get('text','').casefold()==label.casefold() or n.get('content-desc','').casefold()==label.casefold():
   bounds=list(map(int,re.findall(r'\d+',n.get('bounds',''))))
   if len(bounds)==4:
    adb('shell','input','tap',str((bounds[0]+bounds[2])//2),str((bounds[1]+bounds[3])//2));time.sleep(2);return True
 return False

def has_label(xml, label):
 try:
  return any(label.casefold() in (n.get('text','').casefold(), n.get('content-desc','').casefold())
             for n in ET.fromstring(xml).iter('node'))
 except ET.ParseError: return False

def dismiss_startup(xml):
 # Multiple asynchronous first-run dialogs can appear in sequence. Re-read after each tap.
 for attempt in range(12):
  if has_label(xml, 'Home') and has_label(xml, 'Library'): return xml
  if has_label(xml, 'Do not show again'):
   click_label(xml, 'Do not show again')
   xml=capture(f'dialog-{attempt}-checkbox')
  for label in ('Cancel', 'Not now', 'Skip', 'OK', 'Continue', 'Allow'):
   if click_label(xml, label): break
  time.sleep(2)
  xml=capture(f'dialog-{attempt}')
 return xml

adb('logcat','-c')
adb('shell','pm','grant',pkg,'android.permission.POST_NOTIFICATIONS',check=False)
launch=adb('shell','am','start','-W','-n',pkg+'/com.maxrave.simpmusic.MainActivity')
(out/'launch.txt').write_bytes(launch)
time.sleep(15)
xml=capture('01-launch')
xml=dismiss_startup(xml)
assert adb('shell','pidof',pkg,check=False).strip(), 'App process exited after launch'
results=[]
for label in ('Search','Library','Home'):
 clicked=click_label(xml,label)
 time.sleep(2)
 xml=capture('screen-'+label.lower())
 results.append(f'{label}: '+('opened' if clicked else 'control not found'))
logs=adb('logcat','-d','-s','AndroidRuntime:E','ActivityManager:E')
(out/'runtime-errors.txt').write_bytes(logs)
(out/'navigation.txt').write_text('\n'.join(results))
assert b'FATAL EXCEPTION' not in logs and b'ANR in com.alok.dhunora' not in logs, 'Android crash or ANR detected'
assert adb('shell','pidof',pkg,check=False).strip(), 'App process exited during navigation'
assert all(result.endswith(': opened') for result in results), '; '.join(results)
print('Release-code launch passed; navigation evidence:', '; '.join(results))
