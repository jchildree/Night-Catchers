from pathlib import Path
import re
p = Path('build_result.log')
text = p.read_text('utf-16')
for line in text.splitlines():
    if re.search(r'Unresolved reference|error:|Compilation error|Caused by|FAILURE|Exception|MasterKey|Inject|LensId|Domain|Unresolved', line):
        print(line)
