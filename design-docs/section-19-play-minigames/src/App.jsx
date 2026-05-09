import { useState } from "react";

const T = {
  bg:"#09090f",card:"#0f0f1a",card2:"#14142a",border:"rgba(255,255,255,0.08)",
  text:"#f0f0f0",mid:"#b0b0c8",muted:"#606080",dim:"#404060",
  green:"#39FF14",lime:"#7fff00",cyan:"#00ffe7",lavender:"#c77dff",
  rose:"#ff6b9d",gold:"#ffd166",sky:"#72efdd",coral:"#ff6b6b",
  peach:"#ffb347",mint:"#a8edcd",blue:"#4ea8de",purple:"#9d4edd",
};
const FD="'Creepster','Chiller',cursive";
const FM="'Rajdhani','Oswald',sans-serif";
const FB="'Inter','Roboto',sans-serif";

const GAMES = [
  {
    id:"food-toss", name:"Food Toss", emoji:"🍖", tier:"BONDING",
    theme:"ARC PHYSICS · DRAG & RELEASE", themeColor:T.peach, statColor:T.coral,
    linkedStat:"Hunger + Happiness", trigger:"Feed button from Pet Room",
    duration:"3 throws · ~30s", energy:"−10",
    rewards:{hunger:"+25",happiness:"+10"},
    ghostbustersRef:"Feeding Slimer — the glutton of the ghost world",
    ifRef:"\"If\" — the way imaginary friends light up when you bring them food",
    tagline:"Drag to aim. Release to fling. Land food in the monster's open mouth.",
    description:"The monster sits at the top of the screen, mouth gaping open with a hunger expression. A food item appears at the bottom. The player drag-pulls backward (slingshot style) to set arc angle and power, then releases. The food item flies a Bézier arc toward the monster. Hit the open mouth = successful feed. Miss = the monster's eyes droop sadly. 3 throws per session — make them count.",
    mechanics:[
      {label:"Slingshot input",detail:"Modifier.pointerInput tracks drag from food origin. Offset vector = launchVector. Power clamped to maxPullDistance (120dp). A dotted arc preview renders in real time via Canvas DrawScope."},
      {label:"Bézier flight",detail:"Release captures launchVector magnitude + direction. animateOffsetAsState drives food along a cubic Bézier from origin → computed landing point. Control points offset vertically for arc height."},
      {label:"Hit detection",detail:"Each frame checks Rect intersection between foodBounds and mouthBounds. Mouth bounding box updates each frame from monster Lottie frame data (exported as JSON landmark offsets)."},
      {label:"Food variety",detail:"FoodItem sealed class: Bug, Slimeball, GlowSnack, MoonCookie, SockBall. Each has a nomSfxRes and a hungerValue. SockBall is Sock Thief archetype's favourite (+bonus happiness)."},
      {label:"\"Too Full\" state",detail:"If Hunger ≥ 90 when Feed is triggered: monster covers belly, shakes head. FoodTossUseCase returns TooFullResult. No game launched. Child sees a funny refusal animation, never an error."},
      {label:"Miss feedback",detail:"Miss: food splats on floor via Canvas splat decal. Monster covers eyes + whimper SFX. Never angry — goofy sadness only. Floor decals fade after 2s."},
    ],
    compose:["Canvas DrawScope: arc preview dotted line","animateOffsetAsState Bézier food flight","Modifier.pointerInput: drag + release","LottieAnimation: monster nom/miss reactions","SoundPool: per-archetype nom SFX","FoodSplatParticle via Canvas drawCircle scatter"],
    usecases:["FoodTossUseCase — validates hit, resolves food type bonus, checks TooFull","UpdatePetStatsUseCase — hunger/happiness delta","FoodTossSessionEntity — Room: throwCount, hitCount, foodTypes, timestamp"],
    viewmodel:"FoodTossViewModel: foodState: StateFlow<FoodState>, monsterMouthRect: StateFlow<Rect>, throwsRemaining: StateFlow<Int>, onDragStart/onDragUpdate/onDragRelease(offset)",
    accessibility:"TalkBack: 'Food item. Drag up and left to aim at monster mouth above.' Mouth position announced as compass direction. Haptic tick on successful arc release.",
    difficulty:"Gentle: larger mouth hitbox (1.8×), slower arc, 5 throws. Standard: as above. Hard: monster bobs side-to-side, mouth hitbox shrinks to 0.7×.",
  },
  {
    id:"spook-tag", name:"Spook Tag", emoji:"🚪", tier:"BONDING",
    theme:"REACTION · HIDE & SEEK", themeColor:T.purple, statColor:T.purple,
    linkedStat:"Spookiness + Happiness", trigger:"Tap 'Play' from Pet Room",
    duration:"5 rounds · ~45s", energy:"−20",
    rewards:{spookiness:"+15",happiness:"+20"},
    ghostbustersRef:"Ghost hiding behind doors — every haunted house staple",
    ifRef:"The way imaginary friends love to play peek-a-boo",
    tagline:"The monster hides behind one of three doors. Tap the right one before it moves.",
    description:"Three doors are displayed side-by-side. The monster darts between them with a fast shuffle animation. The player must tap the door the monster hid behind before it shuffles again. 5 rounds, getting faster. Correct = door blows open, monster jumps out giggling. Wrong = door creaks open revealing empty shadows. Higher rounds unlock a feint mechanic: the monster fakes a shuffle without moving.",
    mechanics:[
      {label:"Door shuffle",detail:"Monster sprite animates from door A → B (or C) via animateOffsetAsState with spring dampening. Shuffle takes 350ms at round 1, scaling to 180ms at round 5. Door highlights briefly on monster arrival."},
      {label:"Tap detection",detail:"Each door is a Composable with Modifier.clickable (debounced 200ms). On tap: DoorTapEvent(doorIndex) sent to ViewModel. ViewModel resolves correct door from monsterDoorIndex state."},
      {label:"Feint mechanic",detail:"Round 3+: 20% chance of feint. Monster starts shuffle animation (door crack SFX) but snaps back to same door after 80ms. Players who react to sound get punished — teaches observation over reaction."},
      {label:"Speed curve",detail:"Round 1: 3.5s window. Round 2: 2.8s. Round 3: 2.2s. Round 4: 1.6s. Round 5: 1.1s. Miss window = auto-wrong result. Window shown as a draining glow border on each door."},
      {label:"Archetype variation",detail:"Shadow Wisp variant: doors are semi-transparent, monster silhouette only. Closet Creep variant: 4 doors at round 4+. Each archetype has a unique door slam SFX."},
      {label:"Score & streaks",detail:"5 correct in a row = Perfect Round bonus: Spookiness +5 extra. Monster does a victory twirl. 3 wrong in a row: monster peeks out helpfully (charity hint) — never frustrating for children."},
    ],
    compose:["AnimatedVisibility + spring for door open/close","animateOffsetAsState for monster shuffle path","Modifier.clickable with 200ms debounce per door","Canvas: draining timer ring around active door","SoundPool: door creak, slam, giggle SFX per archetype","LottieAnimation: monster peek-out + victory twirl"],
    usecases:["SpookTagRoundUseCase — generates shuffle sequence, validates tap","SpookTagFeintUseCase — probabilistic feint injection by round","UpdatePetStatsUseCase — spookiness/happiness delta"],
    viewmodel:"SpookTagViewModel: doors: StateFlow<List<DoorState>>, monsterDoor: StateFlow<Int>, round: StateFlow<Int>, timeWindow: StateFlow<Float>, onDoorTapped(index)",
    accessibility:"TalkBack: doors announced as 'Left door', 'Middle door', 'Right door'. Monster location announced after each round result. SFX carries directional audio panning matching door position.",
    difficulty:"Gentle: 5s window, no feints, charity hint from round 1. Standard: as above. Hard: 4 doors from round 1, feints from round 2, no charity hint.",
  },
  {
    id:"cuddle-storm", name:"Cuddle Storm", emoji:"💚", tier:"BONDING",
    theme:"TAP FRENZY · AFFECTION", themeColor:T.rose, statColor:T.gold,
    linkedStat:"Trust + Happiness", trigger:"Cuddle button from Pet Room",
    duration:"10 seconds", energy:"−15",
    rewards:{trust:"+8",happiness:"+30",spookiness:"−10"},
    ghostbustersRef:"Slimer getting affection — that moment of surprising warmth",
    ifRef:"\"If\" — the core thesis: imaginary friends flourish when remembered and loved",
    tagline:"Tap the monster as fast as you can. Every tap is a hug. 10 seconds.",
    description:"The monster fills most of the screen, idle and awaiting. The child taps it rapidly — every tap spawns a heart or star burst at the tap point, a micro-haptic pulse, and an incremental happy animation layer. The monster's expression shifts from neutral → pleased → overjoyed as tap count accumulates. The faster the taps, the more confetti fills the screen. Spookiness drains as warmth overwhelms it — the \"If\" monster becoming a friend.",
    mechanics:[
      {label:"Tap registration",detail:"Modifier.pointerInput with PointerEventType.Press. Each distinct pointer down = one tap. Multi-touch supported (both thumbs = 2× taps/s). Debounce: none — raw tap count."},
      {label:"Heart burst particles",detail:"Each tap spawns 6–8 Canvas particles at tap offset. Each particle: random direction vector, 0.8s lifetime, alpha fade + scale shrink. Colours cycle: rose → lavender → gold → mint."},
      {label:"Emotion escalation",detail:"Tap threshold tiers: 0–10 = Neutral, 11–25 = Pleased (cheeks appear), 26–45 = Happy (wiggle), 46+ = Overjoyed (full bounce + confetti rain). Each tier triggers Lottie emotion overlay swap."},
      {label:"Spookiness drain",detail:"Each 5 taps = Spookiness −1 applied at session end (capped at −10 total). Thematic: you're hugging the scary out of it. Closet Creep archetype resists: only −5 max."},
      {label:"Confetti rain",detail:"At 46+ taps: ConfettiSystem activates. Canvas-rendered coloured rectangles falling with random rotation and velocity. Performance-capped at 80 particles on Tier C devices."},
      {label:"Final score card",detail:"Session ends at 10s. Score card shows total taps, max tap/s, hearts given. Monster holds a sign showing the count. Shareable via ShareCompat (parent-gated in V2)."},
    ],
    compose:["Modifier.pointerInput multi-touch tap detection","Canvas particle system: hearts, stars, confetti","LottieAnimation: layered emotion overlay swap","animateFloatAsState: monster scale bounce on each tap","VibrationEffect.createOneShot(20ms) per tap","CountdownTimerBar: animateFloatAsState over 10s"],
    usecases:["CuddleStormUseCase — tallies taps, calculates stat delta, spookiness drain logic","UpdatePetStatsUseCase — trust/happiness/spookiness delta","CuddleStormSessionEntity — Room: tapCount, maxTapRate, timestamp"],
    viewmodel:"CuddleStormViewModel: tapCount: StateFlow<Int>, emotionTier: StateFlow<EmotionTier>, timeRemaining: StateFlow<Float>, isActive: StateFlow<Boolean>, onTap(offset)",
    accessibility:"TalkBack: game described as 'Tap anywhere on screen to show affection. 10 seconds.' Progress announced at each emotion tier change. Result announced with total hugs given.",
    difficulty:"Gentle: 15s window, lower tier thresholds (5/15/30/40), no Spookiness drain. Standard: as above. Hard: hit zones shrink each tier — only monster body counts, not background.",
  },
  {
    id:"slime-sort", name:"Slime Sort", emoji:"🫧", tier:"SKILL",
    theme:"COLOR · SHAPE MATCH", themeColor:T.cyan, statColor:T.lavender,
    linkedStat:"Happiness + Trust", trigger:"Tap 'Play' from Pet Room",
    duration:"60s · 3 rounds", energy:"−15",
    rewards:{happiness:"+20",trust:"+10",spookiness:"−5"},
    ghostbustersRef:"Containment jars from the Ghostbusters firehouse vault",
    ifRef:"The colourful joyful world of IF — sorting is play",
    tagline:"The monster flings slime blobs. Match them to the right containment jar.",
    description:"The monster sits at the top of the screen, excitedly flinging coloured slime blobs downward in arcs. Three Ghostbusters-style containment jars sit at the bottom, each labelled with a shape+colour combo. The player drags each incoming blob to its matching jar before it splats on the floor. Miss three blobs = round ends. Combos multiply score. Phantom blobs appear in round 3 — catch those and they explode in your face.",
    mechanics:[
      {label:"Blob spawn",detail:"AnimatedVisibility + animateOffsetAsState Bézier arc from monster mouth → randomised landing zone. Spawn rate increases each round: R1=2.5s gap, R2=1.8s, R3=1.2s."},
      {label:"Drag-to-jar",detail:"Modifier.pointerInput detects drag start on blob bounding box. Blob follows finger offset. On release: hit-test against jar bounding boxes. Blob snaps into jar on match, splats on miss."},
      {label:"Match logic",detail:"BlobEntity has (color: BlobColor, shape: BlobShape). JarEntity holds (acceptsColor, acceptsShape). Match = color AND shape both correct. Wrong-colour + right-shape = partial (no score, no miss)."},
      {label:"Combo system",detail:"3+ correct in a row → ComboMultiplier ×2. Monster does a happy wiggle (Lottie keyframe). VibrationEffect.createWaveform([0,50,50,80]) on combo. Breaks on any miss."},
      {label:"Phantom blobs",detail:"Round 3: phantom blobs (semi-transparent, glitchy shader overlay). They have no correct jar — dragging to any jar causes a splat-explosion on the monster's face. Hilarious, not punishing."},
      {label:"Round scaling",detail:"R1: 2 colours, 2 shapes. R2: 3 colours, 3 shapes. R3: 4 colours, 4 shapes + phantoms. Shape set: circle, star, hexagon, triangle."},
    ],
    compose:["Canvas + DrawScope for blob arc trails","Modifier.pointerInput drag detection","animateOffsetAsState Bézier blob flight","AnimatedVisibility + glow for jar hover state","VibrationEffect.createWaveform for combo haptic","LottieAnimation: monster wiggle + jar fill animations"],
    usecases:["SlimeSortUseCase — match validation, combo multiplier logic","UpdatePetStatsUseCase — happiness/trust delta on session end","SlimeSortSessionEntity — Room: score, combos, misses, timestamp"],
    viewmodel:"SlimeSortViewModel: blobs: StateFlow<List<BlobState>>, jars: StateFlow<List<JarState>>, combo: StateFlow<Int>, score: StateFlow<Int>, onBlobReleased(blobId, jarId)",
    accessibility:"TalkBack: each blob announced as 'Red circle blob — drag to match'. Jars labelled by colour + shape name. Colour-blind mode: shapes use patterns + icons, not colour alone.",
    difficulty:"Gentle: slower arcs, 2 shapes max, 2× catch window, no phantoms. Standard: as above. Hard: jars rotate positions between rounds.",
  },
  {
    id:"ghost-dash", name:"Ghost Dash", emoji:"👻", tier:"SKILL",
    theme:"REFLEX · PHASE RUNNER", themeColor:T.lavender, statColor:T.purple,
    linkedStat:"Spookiness + Energy", trigger:"Tap 'Play' from Pet Room",
    duration:"Endless · high-score format", energy:"−20",
    rewards:{happiness:"+15",spookiness:"+25",trust:"+5"},
    ghostbustersRef:"Class V full-roaming vapour — phasing through walls",
    ifRef:"Invisible friends who walk unseen through the world",
    tagline:"Hold to go invisible and phase through obstacles. Release to be solid and score.",
    description:"A horizontal side-scroller. The monster auto-runs right. Obstacles scroll left: ghost traps, proton beams, and PKE scanners. Hold the screen to activate Ghost Mode — the monster turns translucent and phases through most obstacles, but the Visibility Meter drains. Release to go solid: meter recharges but you're hittable. The twist: ProtonBeam obstacles can only be dodged while solid. Forces strategic meter management.",
    mechanics:[
      {label:"Phase toggle",detail:"PointerEventType.Press → isInvisible=true. PointerEventType.Release → isInvisible=false. Monster alpha: 1.0 → 0.18 over 200ms. Still faintly visible to child (ghost trail effect) but obstacles can't detect it."},
      {label:"Visibility meter",detail:"maxVisibility=100f. Invisible: −2f/frame. Solid: +1f/frame. At 0f: forced solid for 1.5s (can't phase — penalty). Rendered as a PKE-meter needle readout at the top of screen."},
      {label:"Obstacle types",detail:"GhostTrap: hits solid monster only. ProtonBeam: horizontal beam that hits invisible monster only (beam detects spectral energy). PKEScanner: hits both — telegraphed by 1.5s warning beep. Ratio escalates with distance."},
      {label:"ProtonBeam mechanic",detail:"Unique design: the one obstacle that punishes holding ghost mode. Player must release at the right moment to go solid and duck under/time the gap. Creates the core skill loop."},
      {label:"Score system",detail:"Distance: +1pt/frame. Solid bonus: +2pt/frame while solid and alive (rewards not holding forever). Milestone rings at 100/250/500/1000m with SFX fanfare."},
      {label:"Speed escalation",detail:"Obstacle gap: 1.2s → 0.4s at 500m. Scroll speed: 4dp/frame → 7dp/frame at 1000m. Monster sprite wobble intensity scales with speed. New personal best triggers confetti + monster victory animation."},
    ],
    compose:["Canvas game loop via LaunchedEffect + withFrameMillis","DrawScope.withTransform alpha for ghost phase effect","Modifier.pointerInput hold/release phase toggle","animateFloatAsState for PKE meter needle","SoundPool: phase SFX, hit SFX, milestone chimes","HighScoreEntity — Room for local personal best"],
    usecases:["GhostDashTickUseCase — per-frame: obstacle movement, collision check, meter update","SaveHighScoreUseCase — persists score if new personal best","UpdatePetStatsUseCase — spookiness/energy delta on session end"],
    viewmodel:"GhostDashViewModel: gameState: StateFlow<GhostDashState>, monsterAlpha: StateFlow<Float>, visibilityMeter: StateFlow<Float>, score: StateFlow<Int>, onPhaseDown(), onPhaseUp(), onTick(deltaMs)",
    accessibility:"One-touch input. TalkBack: game incompatible — shows graceful message + awards half stat reward anyway. Colour-blind: obstacle types use shape + icon badges, not colour alone.",
    difficulty:"Gentle: ProtonBeam obstacles removed, meter drains at 50% rate, 3 lives. Standard: ProtonBeam = try-again stun (−50pts, meter flush), GhostTrap/PKEScanner = run ends. Hard: ProtonBeam stun penalty doubled (−100pts, 3s forced solid); reversed beam variant added (must be invisible to pass).",
  },
  {
    id:"proton-wrangle", name:"Proton Wrangle", emoji:"⚡", tier:"SKILL",
    theme:"PHYSICS · BEAM SKILL", themeColor:T.green, statColor:T.gold,
    linkedStat:"Trust + Spookiness", trigger:"Tap 'Play' from Pet Room",
    duration:"90s par time · fastest wins", energy:"−25",
    rewards:{happiness:"+10",trust:"+20",spookiness:"+10"},
    ghostbustersRef:"The proton stream wrangle — the whole thesis of Ghostbusters",
    ifRef:"Taming something wild by earning its trust, not forcing it",
    tagline:"Hold to fire your proton stream. Drag slowly to herd the monster into the trap.",
    description:"The monster floats chaotically. A ghost trap sits open at the bottom. Press and hold to fire a proton stream — a glowing jagged arc from your finger to the monster. While locked, drag slowly to herd it toward the trap. The monster resists with personality-driven force. Too fast = beam snaps (teaches patience). Coax it fully into the trap circle for 1.5s to win. The mechanic is a physical metaphor for trust: gentleness wins, brute force fails.",
    mechanics:[
      {label:"Beam lock-on",detail:"PointerDown → distance check to monsterHitRadius (80dp). Within radius: BeamState.LOCKED. Outside: BeamState.MISSED — spark effect, 1s cooldown before retry. Beam rendered as a jagged Canvas cubicTo path with random micro-offsets per frame."},
      {label:"Monster physics",detail:"MonsterBody: position: Offset, velocity: Offset. Each frame: velocity += resistanceForce (escape vector from trap). Beam adds attractionForce toward drag point. Net velocity clamped to maxSpeed. Full Euler integration."},
      {label:"Resistance personality",detail:"wrangleResistance: Float per archetype. Dust Bunny: 0.3 (gentle). Bed Lurker: 0.5. Sock Thief: 0.8. Closet Creep: 1.0. Door Rattler: 1.3 (snaps beam on jerk). Shadow Wisp: 0.6 but teleports 10% of frames."},
      {label:"Beam snap",detail:"If dragVelocity > snapThreshold (120dp/s): beam snaps. Electric crackle SFX. Monster flees to screen edge. 2s cooldown. Each snap adds +5s to capture time. Teaches children patience is the mechanic."},
      {label:"Trap zone",detail:"Trap rendered at bottom centre. magneticRadius=90dp. Monster inside radius: trap animates wider, beam glow shifts gold. Full containment = monster fully inside for 1.5s → trap slams shut (Lottie sequence)."},
      {label:"Star rating",detail:"90s par. <60s: ⭐⭐⭐. <75s: ⭐⭐. <90s: ⭐. >90s: ⭐ (never 0 — always rewarded). Stars unlock cosmetic beam colours stored in DataStore: green → cyan → gold."},
    ],
    compose:["Canvas DrawScope: jagged beam via cubicTo with random offsets","Custom physics loop via withFrameMillis in LaunchedEffect","Modifier.pointerInput: DOWN (lock), MOVE (drag), UP (release)","animateColorAsState: beam heat-up from green → white","Particle system: Canvas spark scatter on snap","LottieAnimation: trap open sequence + slam on capture"],
    usecases:["ProtonWrangleTickUseCase — physics step: forces, integration, snap detection","WrangleCaptureUseCase — validates containment duration, awards stars","UpdatePetStatsUseCase — trust/spookiness delta with wrangleResistance multiplier"],
    viewmodel:"ProtonWrangleViewModel: monsterPos: StateFlow<Offset>, beamState: StateFlow<BeamState>, trapProgress: StateFlow<Float>, elapsedMs: StateFlow<Long>, onPointerDown(pos), onPointerMove(pos), onPointerUp()",
    accessibility:"TalkBack: monster position as compass direction from trap ('Monster is north-east, guide it south-west'). Haptic: lock-on buzz, snap crackle, capture rumble. Never requires visual precision alone.",
    difficulty:"Gentle: resistance halved, snap threshold doubled, 180s timer, 3 lives. Standard: as above. Hard: 2 monsters simultaneously, trap only opens every 20s.",
  },
];

const STAT_MATRIX = [
  {stat:"Hunger",    icon:"🍖",color:T.coral,  ft:"+25",st:"—",   cs:"—",  ss:"—",  gd:"—",   pw:"—"},
  {stat:"Happiness", icon:"😊",color:T.rose,   ft:"+10",st:"+20", cs:"+30",ss:"+20",gd:"+15", pw:"+10"},
  {stat:"Trust",     icon:"💛",color:T.gold,   ft:"—",  st:"—",   cs:"+8", ss:"+10",gd:"+5",  pw:"+20"},
  {stat:"Spookiness",icon:"👻",color:T.purple, ft:"—",  st:"+15", cs:"−10",ss:"−5", gd:"+25", pw:"+10"},
  {stat:"Energy",    icon:"⚡",color:T.cyan,   ft:"−10",st:"−20", cs:"−15",ss:"−15",gd:"−20", pw:"−25"},
];

const TIER_COLORS = { BONDING: T.rose, SKILL: T.green };

// ── COMPONENTS ────────────────────────────────────────────────────────────────
function Tag({ label, color }) {
  return (
    <span style={{
      fontFamily:FM,fontSize:9,fontWeight:700,letterSpacing:"0.12em",
      padding:"3px 8px",borderRadius:3,
      background:`${color}18`,color,border:`1px solid ${color}44`,
    }}>{label}</span>
  );
}
function SLabel({ children, color=T.muted }) {
  return <div style={{fontFamily:FM,fontSize:10,color,letterSpacing:"0.1em",marginBottom:8}}>{children}</div>;
}
function Dot({ color }) {
  return <div style={{width:5,height:5,borderRadius:"50%",background:color,flexShrink:0,marginTop:5}}/>;
}
function RewardPill({ label, value, color }) {
  const neg = value.startsWith("−")||value.startsWith("-");
  return (
    <div style={{
      display:"inline-flex",alignItems:"center",gap:5,
      background:`${neg?T.coral:color}14`,border:`1px solid ${neg?T.coral:color}44`,
      borderRadius:20,padding:"4px 10px",marginRight:6,marginBottom:5,
    }}>
      <span style={{fontFamily:FM,fontSize:10,color:T.muted}}>{label}</span>
      <span style={{fontFamily:FM,fontSize:11,fontWeight:700,color:neg?T.coral:color}}>{value}</span>
    </div>
  );
}
function MechanicRow({ label, detail }) {
  const [open,setOpen]=useState(false);
  return (
    <div
      onClick={()=>setOpen(p=>!p)}
      style={{
        background:open?T.card2:T.card,border:`1px solid ${T.border}`,
        borderRadius:7,padding:"10px 14px",marginBottom:6,cursor:"pointer",
        transition:"background 0.15s",
      }}
    >
      <div style={{display:"flex",justifyContent:"space-between",alignItems:"center"}}>
        <span style={{fontFamily:FM,fontSize:12,color:T.cyan,letterSpacing:"0.06em"}}>{label}</span>
        <span style={{fontFamily:FM,fontSize:10,color:T.muted}}>{open?"▲":"▼"}</span>
      </div>
      {open&&<p style={{fontFamily:FB,fontSize:12,color:T.mid,marginTop:8,marginBottom:0,lineHeight:1.6}}>{detail}</p>}
    </div>
  );
}

function GameCard({ game, expanded, onToggle }) {
  const tierColor = TIER_COLORS[game.tier];
  return (
    <div
      style={{
        background:expanded?T.card2:T.card,
        border:`1px solid ${expanded?`${game.themeColor}55`:T.border}`,
        borderRadius:12,padding:"20px 22px",cursor:"pointer",
        transition:"all 0.2s",
        boxShadow:expanded?`0 0 28px ${game.themeColor}18`:"none",
        marginBottom:14,
      }}
    >
      {/* HEADER */}
      <div onClick={onToggle} style={{display:"flex",gap:14,alignItems:"center"}}>
        <div style={{
          width:50,height:50,borderRadius:14,flexShrink:0,
          background:`${game.themeColor}18`,border:`1px solid ${game.themeColor}44`,
          display:"flex",alignItems:"center",justifyContent:"center",fontSize:26,
        }}>{game.emoji}</div>
        <div style={{flex:1,minWidth:0}}>
          <div style={{display:"flex",gap:8,alignItems:"center",flexWrap:"wrap",marginBottom:5}}>
            <span style={{fontFamily:FD,fontSize:22,color:T.text}}>{game.name}</span>
            <Tag label={game.tier} color={tierColor}/>
            <Tag label={game.theme} color={game.themeColor}/>
          </div>
          <div style={{fontFamily:FM,fontSize:11,color:T.muted}}>
            {game.linkedStat} · {game.duration} · Energy {game.energy}
          </div>
        </div>
        <div style={{fontFamily:FM,fontSize:11,color:T.muted,flexShrink:0}}>{expanded?"▲":"▼"}</div>
      </div>

      {/* TAGLINE */}
      {!expanded&&(
        <p style={{fontFamily:FB,fontSize:13,color:T.muted,marginTop:12,marginBottom:0,fontStyle:"italic"}}>&ldquo;{game.tagline}&rdquo;</p>
      )}

      {expanded&&(
        <div style={{marginTop:20}} onClick={e=>e.stopPropagation()}>
          {/* INSPIRATION */}
          <div style={{display:"grid",gridTemplateColumns:"1fr 1fr",gap:10,marginBottom:18}}>
            {[{label:"👻 GHOSTBUSTERS",text:game.ghostbustersRef,c:T.green},{label:"🎨 IF (2024)",text:game.ifRef,c:T.lavender}].map(r=>(
              <div key={r.label} style={{background:"rgba(255,255,255,0.02)",border:`1px solid ${T.border}`,borderRadius:8,padding:"12px 14px"}}>
                <SLabel color={r.c}>{r.label}</SLabel>
                <p style={{fontFamily:FB,fontSize:12,color:T.mid,margin:0,lineHeight:1.55}}>{r.text}</p>
              </div>
            ))}
          </div>

          {/* DESCRIPTION */}
          <div style={{background:`${game.themeColor}08`,border:`1px solid ${game.themeColor}22`,borderRadius:8,padding:"14px 16px",marginBottom:18}}>
            <SLabel color={game.themeColor}>OVERVIEW</SLabel>
            <p style={{fontFamily:FB,fontSize:13,color:T.mid,margin:0,lineHeight:1.65}}>{game.description}</p>
          </div>

          {/* REWARDS */}
          <div style={{marginBottom:18}}>
            <SLabel color={game.statColor}>STAT REWARDS</SLabel>
            <div style={{display:"flex",flexWrap:"wrap"}}>
              {Object.entries(game.rewards).map(([k,v])=>(
                <RewardPill key={k} label={k.charAt(0).toUpperCase()+k.slice(1)} value={v} color={game.statColor}/>
              ))}
              <RewardPill label="Energy" value={game.energy} color={T.cyan}/>
            </div>
          </div>

          {/* MECHANICS */}
          <div style={{marginBottom:18}}>
            <SLabel color={T.cyan}>MECHANICS</SLabel>
            {game.mechanics.map(m=><MechanicRow key={m.label} label={m.label} detail={m.detail}/>)}
          </div>

          {/* IMPL */}
          <div style={{display:"grid",gridTemplateColumns:"1fr 1fr",gap:10,marginBottom:18}}>
            <div style={{background:T.card,border:`1px solid ${T.border}`,borderRadius:8,padding:"12px 14px"}}>
              <SLabel color={T.green}>COMPOSE APIs</SLabel>
              {game.compose.map((c,i)=>(
                <div key={i} style={{display:"flex",gap:8,marginBottom:5}}>
                  <Dot color={T.green}/><span style={{fontFamily:FB,fontSize:11,color:T.mid}}>{c}</span>
                </div>
              ))}
            </div>
            <div style={{background:T.card,border:`1px solid ${T.border}`,borderRadius:8,padding:"12px 14px"}}>
              <SLabel color={T.gold}>USE CASES + ROOM</SLabel>
              {game.usecases.map((u,i)=>(
                <div key={i} style={{display:"flex",gap:8,marginBottom:5}}>
                  <Dot color={T.gold}/><span style={{fontFamily:FB,fontSize:11,color:T.mid}}>{u}</span>
                </div>
              ))}
            </div>
          </div>

          {/* VIEWMODEL */}
          <div style={{background:"rgba(0,255,231,0.04)",border:`1px solid ${T.cyan}22`,borderRadius:8,padding:"12px 14px",marginBottom:14}}>
            <SLabel color={T.cyan}>VIEWMODEL INTERFACE</SLabel>
            <code style={{fontFamily:"monospace",fontSize:11,color:T.sky,lineHeight:1.7,display:"block",whiteSpace:"pre-wrap",wordBreak:"break-all"}}>{game.viewmodel}</code>
          </div>

          {/* ACCESSIBILITY + DIFFICULTY */}
          <div style={{display:"grid",gridTemplateColumns:"1fr 1fr",gap:10}}>
            {[
              {label:"♿ ACCESSIBILITY",text:game.accessibility,c:T.mint},
              {label:"🎮 DIFFICULTY MODES",text:game.difficulty,c:T.peach},
            ].map(r=>(
              <div key={r.label} style={{background:T.card,border:`1px solid ${T.border}`,borderRadius:8,padding:"12px 14px"}}>
                <SLabel color={r.c}>{r.label}</SLabel>
                <p style={{fontFamily:FB,fontSize:12,color:T.mid,margin:0,lineHeight:1.55}}>{r.text}</p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

// ── APP ───────────────────────────────────────────────────────────────────────
export default function App() {
  const [expanded, setExpanded] = useState(null);
  const [view, setView] = useState("games");

  const bonding = GAMES.filter(g=>g.tier==="BONDING");
  const skill = GAMES.filter(g=>g.tier==="SKILL");

  return (
    <div style={{background:T.bg,minHeight:"100vh",fontFamily:FB,padding:"24px 20px",maxWidth:860,margin:"0 auto"}}>
      {/* HEADER */}
      <div style={{marginBottom:32}}>
        <div style={{display:"flex",alignItems:"baseline",gap:12,marginBottom:6}}>
          <span style={{fontFamily:FM,fontSize:11,color:T.green,letterSpacing:"0.12em"}}>SECTION 19 —</span>
          <h1 style={{fontFamily:FD,fontSize:36,color:T.text,margin:0}}>Play Feature · Mini-Games</h1>
        </div>
        <p style={{fontFamily:FM,fontSize:10,color:T.muted,letterSpacing:"0.08em",margin:"0 0 14px"}}>
          6 GAMES · COMPOSE CANVAS · ENERGY-GATED · GHOSTBUSTERS × IF · CHILD-FIRST DESIGN
        </p>
        <div style={{height:1,background:`linear-gradient(90deg,${T.green}66,transparent)`}}/>
      </div>

      {/* NAV */}
      <div style={{display:"flex",gap:8,marginBottom:28,flexWrap:"wrap"}}>
        {[["games","🎮 All Games"],["matrix","📊 Stat Matrix"],["rotation","🔄 Daily Rotation"],["navgraph","🗺️ Nav Graph"]].map(([id,label])=>(
          <button key={id} onClick={()=>setView(id)} style={{
            fontFamily:FM,fontSize:11,letterSpacing:"0.08em",
            padding:"8px 16px",borderRadius:6,cursor:"pointer",border:"none",
            background:view===id?T.green:"rgba(255,255,255,0.05)",
            color:view===id?T.bg:T.muted,
            transition:"all 0.15s",
          }}>{label}</button>
        ))}
      </div>

      {/* ── GAMES VIEW ── */}
      {view==="games"&&(
        <div>
          <div style={{display:"flex",alignItems:"center",gap:12,marginBottom:16}}>
            <div style={{width:8,height:8,borderRadius:"50%",background:T.rose}}/>
            <span style={{fontFamily:FM,fontSize:11,color:T.rose,letterSpacing:"0.1em"}}>BONDING GAMES — Lower energy cost · Stat decay recovery · Always available</span>
          </div>
          {bonding.map(g=>(
            <GameCard key={g.id} game={g} expanded={expanded===g.id} onToggle={()=>setExpanded(p=>p===g.id?null:g.id)}/>
          ))}
          <div style={{display:"flex",alignItems:"center",gap:12,marginBottom:16,marginTop:8}}>
            <div style={{width:8,height:8,borderRadius:"50%",background:T.green}}/>
            <span style={{fontFamily:FM,fontSize:11,color:T.green,letterSpacing:"0.1em"}}>SKILL GAMES — Higher energy cost · Larger stat rewards · Daily rotation featured</span>
          </div>
          {skill.map(g=>(
            <GameCard key={g.id} game={g} expanded={expanded===g.id} onToggle={()=>setExpanded(p=>p===g.id?null:g.id)}/>
          ))}
        </div>
      )}

      {/* ── STAT MATRIX ── */}
      {view==="matrix"&&(
        <div>
          <SLabel color={T.gold}>STAT IMPACT MATRIX — All 6 Games</SLabel>
          <div style={{overflowX:"auto"}}>
            <table style={{width:"100%",borderCollapse:"collapse",marginBottom:24}}>
              <thead>
                <tr>
                  <th style={{fontFamily:FM,fontSize:10,color:T.muted,textAlign:"left",padding:"10px 14px",borderBottom:`1px solid ${T.border}`}}>STAT</th>
                  {["Food Toss","Spook Tag","Cuddle Storm","Slime Sort","Ghost Dash","Proton Wrangle"].map(h=>(
                    <th key={h} style={{fontFamily:FM,fontSize:10,color:T.cyan,textAlign:"center",padding:"10px 8px",borderBottom:`1px solid ${T.border}`,letterSpacing:"0.06em"}}>{h.toUpperCase()}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {STAT_MATRIX.map((row,i)=>(
                  <tr key={row.stat} style={{background:i%2===0?"rgba(255,255,255,0.015)":"transparent"}}>
                    <td style={{padding:"12px 14px",display:"flex",alignItems:"center",gap:8}}>
                      <span style={{fontSize:16}}>{row.icon}</span>
                      <span style={{fontFamily:FM,fontSize:12,color:row.color,letterSpacing:"0.06em"}}>{row.stat}</span>
                    </td>
                    {[row.ft,row.st,row.cs,row.ss,row.gd,row.pw].map((v,j)=>{
                      const isNeg=v.startsWith("−")||v.startsWith("-");
                      const isEmpty=v==="—";
                      return (
                        <td key={j} style={{textAlign:"center",padding:"12px 8px"}}>
                          <span style={{
                            fontFamily:FM,fontSize:13,fontWeight:700,
                            color:isEmpty?T.dim:isNeg?T.coral:row.color,
                          }}>{v}</span>
                        </td>
                      );
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div style={{background:`${T.gold}08`,border:`1px solid ${T.gold}22`,borderRadius:8,padding:"14px 16px"}}>
            <SLabel color={T.gold}>DESIGN PRINCIPLE</SLabel>
            <p style={{fontFamily:FB,fontSize:13,color:T.mid,margin:0,lineHeight:1.65}}>
              No single game covers all stats. Trust requires Cuddle Storm or Proton Wrangle.
              Spookiness grows fastest via Ghost Dash, but Cuddle Storm and Slime Sort drain it — creating
              a natural personality toggle the child controls. Hunger is exclusively fed via Food Toss,
              keeping the bonding games distinct in purpose.
            </p>
          </div>
        </div>
      )}

      {/* ── ROTATION ── */}
      {view==="rotation"&&(
        <div>
          <SLabel color={T.cyan}>DAILY VARIETY SYSTEM</SLabel>
          <div style={{background:T.card2,border:`1px solid ${T.border}`,borderRadius:10,padding:"20px 22px",marginBottom:18}}>
            <div style={{display:"grid",gridTemplateColumns:"1fr 1fr 1fr",gap:14,marginBottom:18}}>
              {[
                {day:"dayOfYear % 6 = 0",featured:"Food Toss",color:T.peach},
                {day:"dayOfYear % 6 = 1",featured:"Spook Tag",color:T.purple},
                {day:"dayOfYear % 6 = 2",featured:"Cuddle Storm",color:T.rose},
                {day:"dayOfYear % 6 = 3",featured:"Slime Sort",color:T.cyan},
                {day:"dayOfYear % 6 = 4",featured:"Ghost Dash",color:T.lavender},
                {day:"dayOfYear % 6 = 5",featured:"Proton Wrangle",color:T.green},
              ].map(r=>(
                <div key={r.day} style={{background:T.card,border:`1px solid ${r.color}33`,borderRadius:8,padding:"12px 14px"}}>
                  <div style={{fontFamily:FM,fontSize:9,color:T.muted,letterSpacing:"0.1em",marginBottom:6}}>{r.day}</div>
                  <div style={{fontFamily:FD,fontSize:17,color:r.color}}>{r.featured}</div>
                  <div style={{fontFamily:FM,fontSize:10,color:T.dim,marginTop:4}}>FEATURED</div>
                </div>
              ))}
            </div>
            <p style={{fontFamily:FB,fontSize:13,color:T.mid,margin:0,lineHeight:1.65}}>
              Deterministic rotation via <code style={{color:T.cyan,fontFamily:"monospace"}}>dayOfYear % 6</code>.
              The featured game appears first and gets a glow highlight in the Play menu. All 6 games are always
              available after the daily featured game is played once. Bonding games (Food Toss, Cuddle Storm)
              are <em>always</em> available regardless of energy if core stats are critically low.
            </p>
          </div>
          <div style={{display:"grid",gridTemplateColumns:"1fr 1fr",gap:12}}>
            {[
              {title:"Energy Gate",color:T.cyan,text:"Skill games (Slime Sort, Ghost Dash, Proton Wrangle) are locked if Energy < 25. A gentle message from the monster explains it needs rest. Bonding games remain accessible at Energy ≥ 10."},
              {title:"Critical Stat Override",color:T.coral,text:"If Hunger < 15: Food Toss bypasses Energy gate. If Trust < 10 and monster is newly captured: Cuddle Storm bypasses Energy gate. Child always has a path to care for their pet."},
              {title:"Gentle Mode Overrides",color:T.mint,text:"Gentle Mode disables Energy gating entirely. All games available always. Gentle Mode profiles also receive a 15% stat bonus to compensate for reduced challenge."},
              {title:"Daily Bonus",color:T.gold,text:"First game played each day awards a +5 Happiness bonus regardless of score — the 'just showed up' reward, mirroring the IF theme of presence mattering more than performance."},
            ].map(r=>(
              <div key={r.title} style={{background:T.card,border:`1px solid ${T.border}`,borderRadius:8,padding:"14px 16px"}}>
                <SLabel color={r.color}>{r.title.toUpperCase()}</SLabel>
                <p style={{fontFamily:FB,fontSize:12,color:T.mid,margin:0,lineHeight:1.6}}>{r.text}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* ── NAV GRAPH ── */}
      {view==="navgraph"&&(
        <div>
          <SLabel color={T.lavender}>PetNavGraph — Play Sub-Graph</SLabel>
          <div style={{background:T.card2,border:`1px solid ${T.border}`,borderRadius:10,padding:"20px 22px",marginBottom:18}}>
            <code style={{fontFamily:"monospace",fontSize:12,color:T.sky,lineHeight:1.9,display:"block",whiteSpace:"pre-wrap"}}>
{`// PetNavGraph.kt — Play sub-graph
navigation(
    startDestination = PetRoute.PlayMenu,
    route = PetRoute.PlayRoot
) {
    composable(PetRoute.PlayMenu) {
        PlayMenuScreen(           // shows all 6 games, energy state, featured
            onNavigate = navCtrl::navigate
        )
    }
    composable(PetRoute.FoodToss) {
        FoodTossScreen(
            viewModel = hiltViewModel<FoodTossViewModel>()
        )
    }
    composable(PetRoute.SpookTag) {
        SpookTagScreen(
            viewModel = hiltViewModel<SpookTagViewModel>()
        )
    }
    composable(PetRoute.CuddleStorm) {
        CuddleStormScreen(
            viewModel = hiltViewModel<CuddleStormViewModel>()
        )
    }
    composable(PetRoute.SlimeSort) {
        SlimeSortScreen(
            viewModel = hiltViewModel<SlimeSortViewModel>()
        )
    }
    composable(PetRoute.GhostDash) {
        GhostDashScreen(
            viewModel = hiltViewModel<GhostDashViewModel>()
        )
    }
    composable(PetRoute.ProtonWrangle) {
        ProtonWrangleScreen(
            viewModel = hiltViewModel<ProtonWrangleViewModel>()
        )
    }
    composable(PetRoute.GameResult) { // shared result screen
        GameResultScreen(            // monster reaction + stat delta card
            viewModel = hiltViewModel<GameResultViewModel>()
        )
    }
}`}
            </code>
          </div>
          <div style={{display:"grid",gridTemplateColumns:"1fr 1fr",gap:12}}>
            {[
              {title:"Shared Result Screen",color:T.gold,text:"All 6 games navigate to a shared GameResultScreen passing a GameResultPayload (gameId, score, statDeltas, starsAwarded). This screen shows the monster's reaction animation + a stat delta summary card. One composable, six entrypoints."},
              {title:"Energy Check",color:T.cyan,text:"PlayMenuScreen reads monsterEnergy from PetViewModel. Skill game cards render with a lock overlay and energy tooltip if below threshold. Navigation blocked at the ViewModel level, not just UI."},
              {title:"Back Stack",color:T.lavender,text:"Games use popUpTo(PetRoute.PlayMenu, inclusive=false) on completion so back press from GameResultScreen returns to PlayMenu, not mid-game state. Mid-game back press shows a quit confirmation dialog."},
              {title:"SavedStateHandle",color:T.green,text:"Each game ViewModel uses SavedStateHandle for isGameActive, score, and sessionId. Process death mid-game restores state correctly — no lost progress for children."},
            ].map(r=>(
              <div key={r.title} style={{background:T.card,border:`1px solid ${T.border}`,borderRadius:8,padding:"14px 16px"}}>
                <SLabel color={r.color}>{r.title.toUpperCase()}</SLabel>
                <p style={{fontFamily:FB,fontSize:12,color:T.mid,margin:0,lineHeight:1.6}}>{r.text}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      <div style={{marginTop:36,height:1,background:`linear-gradient(90deg,transparent,${T.green}44,transparent)`}}/>
      <p style={{fontFamily:FM,fontSize:10,color:T.dim,textAlign:"center",marginTop:14,letterSpacing:"0.08em"}}>
        MONSTER CATCHER · SECTION 19 · PLAY FEATURE · 6 MINI-GAMES · GHOSTBUSTERS × IF
      </p>
    </div>
  );
}
