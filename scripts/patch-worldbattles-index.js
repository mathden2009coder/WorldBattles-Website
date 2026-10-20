const fs = require('fs');

const file = 'index.html';
let html = fs.readFileSync(file, 'utf8');
if (html.includes('worldbattles-gallery-16-9-v160')) {
  console.log('The index is already patched.');
  process.exit(0);
}

const marker = '<div class="summary-panel-v77" data-summary-panel="instructions"><p class="summary-instructions-intro-v77">Pour lancer une partie proprement';
const start = html.indexOf(marker);
if (start < 0) throw new Error('WorldBattles/SandWars instructions panel not found.');

const divTag = /<\/?div\b[^>]*>/g;
divTag.lastIndex = start;
let depth = 0;
let end = -1;
let match;
while ((match = divTag.exec(html))) {
  if (/^<div\b/i.test(match[0])) depth++;
  else depth--;
  if (depth === 0) {
    end = divTag.lastIndex;
    break;
  }
}
if (end < 0) throw new Error('Could not find the end of the instructions panel.');

const newPanel = [
'<div class="summary-panel-v77" data-summary-panel="instructions">',
'<p class="summary-instructions-intro-v77"><span class="wb-fr-v150">Règles à connaître avant de rejoindre une partie. Choisis le mode ci-dessous et suis les consignes de ton arène.</span><span class="wb-en-v150">Know the rules before joining a match. Choose your mode below and follow the instructions for your arena.</span></p>',
'<h3 class="summary-rules-heading-v160"><span class="wb-fr-v150">WORLDSBATTLE</span><span class="wb-en-v150">WORLDSBATTLE</span></h3>',
'<div class="summary-steps-v77">',
'<div class="summary-step-v77"><span class="summary-step-num-v77">01</span><span><span class="wb-fr-v150">Rejoins l’arène et choisis ton équipe : Nether ou Overworld.</span><span class="wb-en-v150">Join the arena and choose your team: Nether or Overworld.</span></span></div>',
'<div class="summary-step-v77"><span class="summary-step-num-v77">02</span><span><span class="wb-fr-v150">Seules les éliminations d’ennemis rapportent un point à ton équipe (+1 kill).</span><span class="wb-en-v150">Only enemy eliminations score a point for your team (+1 kill).</span></span></div>',
'<div class="summary-step-v77"><span class="summary-step-num-v77">03</span><span><span class="wb-fr-v150">La première équipe à 25 éliminations remporte la manche. Il faut gagner 2 manches pour remporter la partie.</span><span class="wb-en-v150">The first team to 25 eliminations wins a round. Win 2 rounds to win the match.</span></span></div>',
'<div class="summary-step-v77"><span class="summary-step-num-v77">04</span><span><span class="wb-fr-v150">Après chaque réapparition, tu bénéficies de 5 secondes d’invincibilité.</span><span class="wb-en-v150">After each respawn, you have 5 seconds of invulnerability.</span></span></div>',
'<div class="summary-step-v77"><span class="summary-step-num-v77">05</span><span><span class="wb-fr-v150">Le PvP est désactivé uniquement dans le lobby : les combats restent actifs dans l’arène.</span><span class="wb-en-v150">PvP is disabled only in the lobby; combat remains enabled in the arena.</span></span></div>',
'<div class="summary-step-v77"><span class="summary-step-num-v77">06</span><span><span class="wb-fr-v150">Utilise <code>/spawn</code> pour quitter. La partie se termine s’il reste moins de 2 joueurs.</span><span class="wb-en-v150">Use <code>/spawn</code> to leave. The match ends when fewer than 2 players remain.</span></span></div>',
'</div>',
'<h3 class="summary-rules-heading-v160"><span class="wb-fr-v150">SANDWARS</span><span class="wb-en-v150">SANDWARS</span></h3>',
'<div class="summary-steps-v77">',
'<div class="summary-step-v77"><span class="summary-step-num-v77">01</span><span><span class="wb-fr-v150">Rejoins l’arène SandWars et choisis ton camp.</span><span class="wb-en-v150">Join the SandWars arena and choose your side.</span></span></div>',
'<div class="summary-step-v77"><span class="summary-step-num-v77">02</span><span><span class="wb-fr-v150">Élimine les adversaires : seules les éliminations ennemies comptent (+1 kill).</span><span class="wb-en-v150">Eliminate opponents: only enemy eliminations count (+1 kill).</span></span></div>',
'<div class="summary-step-v77"><span class="summary-step-num-v77">03</span><span><span class="wb-fr-v150">Après chaque réapparition, tu bénéficies de 5 secondes d’invincibilité.</span><span class="wb-en-v150">After each respawn, you have 5 seconds of invulnerability.</span></span></div>',
'<div class="summary-step-v77"><span class="summary-step-num-v77">04</span><span><span class="wb-fr-v150">Le PvP est désactivé uniquement dans le lobby : les combats restent actifs dans l’arène.</span><span class="wb-en-v150">PvP is disabled only in the lobby; combat remains enabled in the arena.</span></span></div>',
'<div class="summary-step-v77"><span class="summary-step-num-v77">05</span><span><span class="wb-fr-v150">Utilise <code>/spawn</code> pour quitter. La partie se termine s’il reste moins de 2 joueurs.</span><span class="wb-en-v150">Use <code>/spawn</code> to leave. The match ends when fewer than 2 players remain.</span></span></div>',
'</div>',
'</div>'
].join('');

html = html.slice(0, start) + newPanel + html.slice(end);

const additions = [
'<!-- WORLDBATTLES V16.0: fixed 16:9 gallery frames and updated arena rules -->',
'<style id="worldbattles-gallery-16-9-v160">',
'.summary-rules-heading-v160{margin:18px 0 9px;color:#f2f5fa;font:400 .94rem/1.25 "Russo One",sans-serif;letter-spacing:.045em}',
'.summary-rules-heading-v160:first-of-type{margin-top:12px}',
'.spa-page[data-route="world"] .world-gallery-category-v101.wb-gallery-upgraded-v150 .wb-gallery-feature-v150{display:block!important;width:100%!important;height:auto!important;min-height:0!important;aspect-ratio:16/9!important;max-height:none!important;background-size:cover!important;background-position:center!important}',
'.spa-page[data-route="world"] .world-gallery-category-v101.wb-gallery-upgraded-v150 .image-strip .image-tile{width:100%!important;height:auto!important;min-height:0!important;aspect-ratio:16/9!important;background-size:cover!important;background-position:center!important;align-self:start}',
'.spa-page[data-route="gallery"] .gallery-grid-v109 .gallery-photo-v109,.gallery-page-v109 .gallery-grid-v109 .gallery-photo-v109{width:100%!important;height:auto!important;min-height:0!important;aspect-ratio:16/9!important;background-size:cover!important;background-position:center!important}',
'.img-lightbox-stage-v107{position:relative!important;width:min(100%,1280px,150vh)!important;height:auto!important;max-height:85vh!important;aspect-ratio:16/9!important;margin-inline:auto!important;display:flex!important;align-items:center!important;justify-content:center!important;overflow:hidden}',
'.img-lightbox-image-v90{display:block!important;width:100%!important;height:100%!important;max-height:none!important;object-fit:contain!important;object-position:center!important;background:transparent!important}',
'@media(max-width:820px){.spa-page[data-route="world"] .world-gallery-category-v101.wb-gallery-upgraded-v150 .image-strip{grid-template-columns:repeat(5,120px)!important}.spa-page[data-route="world"] .world-gallery-category-v101.wb-gallery-upgraded-v150 .image-strip .image-tile{width:120px!important;height:auto!important;aspect-ratio:16/9!important}.img-lightbox-stage-v107{width:min(100%,150vh)!important;aspect-ratio:16/9!important;max-height:85vh!important}}',
'@media(prefers-reduced-motion:reduce){.spa-page[data-route="world"] .world-gallery-category-v101.wb-gallery-upgraded-v150 .image-strip .image-tile{transition:none!important}}',
'</style>'
].join('\n');

const bodyClose = html.toLowerCase().lastIndexOf('</body>');
if (bodyClose < 0) throw new Error('Closing body tag not found.');
html = html.slice(0, bodyClose) + additions + '\n' + html.slice(bodyClose);

if (Buffer.byteLength(html, 'utf8') >= 25 * 1024 * 1024) throw new Error('index.html would exceed the 25 MB limit.');
if (!html.includes('Only enemy eliminations score a point') || !html.includes('aspect-ratio:16/9!important')) throw new Error('Final validation failed.');
fs.writeFileSync(file, html, 'utf8');
console.log('Patched index.html (' + Buffer.byteLength(html, 'utf8') + ' bytes).');
