"""Contract tests for the exact Farlands Metiers V5.4 datapack integration."""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
java = root / 'src/main/java/com/worldbattles/jobs'
read = lambda name: (java / name).read_text(encoding='utf-8')
catalog = read('JobCatalog.java')
snapshot = read('JobSnapshot.java')
network = read('JobsNetwork.java')
screen = read('client/JobsScreen.java')
main = read('WorldBattlesJobs.java')

jobs = {'miner':'m', 'lumber':'l', 'farmer':'f', 'hunter':'h',
        'fisher':'p', 'explorer':'e', 'builder':'b'}
for job, prefix in jobs.items():
    assert f'new Job("{job}"' in catalog, job
    for field in ('lvl','xp','need'):
        assert f'"fj_{prefix}_{field}"' in catalog, f'{job}:{field}'
assert len(re.findall(r'new Job\(', catalog)) == 7
assert catalog.count('"VANILLA"') > 30
assert catalog.count('"MOD PRATIQUE"') > 30
assert 'hasPlayerScore' in snapshot
assert 'getOrCreatePlayerScore' in snapshot and '.getScore()' in snapshot
assert 'setScore' not in snapshot
assert 'scoreboard players' not in '\n'.join([snapshot,network,main])
assert 'JobsClient.accept(snapshot)' in network
assert 'screen.update(snapshot)' in read('client/JobsClient.java')
assert 'requestRefresh()' in screen
assert 'this.width' not in screen or 'width * 84 / 100' in screen
assert 'glfwSetCursorPos' not in screen
assert 'metiers' in main and 'wbmetiers' in main
print('OK: 7 datapack objectives, rewards, read-only sync, cursor-preserving UI')
