# Claude-ITect-Skill v2.0 — Windows Install Script
# Installs all skills, agents, hooks, and commands into a project's .claude directory.
# Also wires caveman hooks into the project's .claude/settings.json.
#
# Usage:
#   .\install.ps1                        # installs into current directory
#   .\install.ps1 -ProjectPath C:\path  # installs into specified project
#   .\install.ps1 -Force                 # overwrite existing skills
#   .\install.ps1 -SkipHooks             # skip settings.json hook wiring
#
# If blocked by execution policy, run via:  install.bat
# Or manually:  powershell -ExecutionPolicy Bypass -File install.ps1

param(
    [string]$ProjectPath = (Get-Location),
    [switch]$Force,
    [switch]$SkipHooks
)

$src    = $PSScriptRoot
$claude = Join-Path $ProjectPath ".claude"

function Copy-Dir($from, $to, $label) {
    if (-not (Test-Path $from)) { return }
    New-Item -ItemType Directory -Force -Path $to | Out-Null
    $copied = 0; $skipped = 0
    Get-ChildItem $from -Directory | ForEach-Object {
        $target = Join-Path $to $_.Name
        if ((Test-Path $target) -and -not $Force) { $skipped++ }
        else { Copy-Item $_.FullName $to -Recurse -Force; $copied++ }
    }
    Write-Host "${label}: installed=$copied  skipped=$skipped"
}

function Copy-Files($from, $to, $label) {
    if (-not (Test-Path $from)) { return }
    New-Item -ItemType Directory -Force -Path $to | Out-Null
    Copy-Item "$from\*" $to -Recurse -Force
    Write-Host "${label}: copied"
}

function Wire-Hooks($hooksDir, $settingsPath) {
    if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
        Write-Host "hooks  : Node.js not found - skipped settings.json wiring"
        Write-Host "         Add hooks manually per CLAUDE.md"
        return
    }

    $activateCmd = "node `"$hooksDir\caveman-activate.js`""
    $trackerCmd  = "node `"$hooksDir\caveman-mode-tracker.js`""

    New-Item -ItemType Directory -Force -Path (Split-Path $settingsPath) | Out-Null

    $utf8NoBom = New-Object System.Text.UTF8Encoding $false
    if (-not (Test-Path $settingsPath)) {
        [System.IO.File]::WriteAllText($settingsPath, "{`"hooks`":{}}`n", $utf8NoBom)
    }

    $nodeScript = @'
const fs = require('fs');
const [,, settingsPath, activateCmd, trackerCmd] = process.argv;
const raw = fs.readFileSync(settingsPath, 'utf8');
const s = JSON.parse(raw.charCodeAt(0) === 0xFEFF ? raw.slice(1) : raw);
s.hooks = s.hooks || {};

function hasHook(arr, cmd) {
    return (arr || []).some(e => (e.hooks || []).some(h => h.command === cmd));
}
function addHook(s, event, cmd) {
    s.hooks[event] = s.hooks[event] || [];
    if (!hasHook(s.hooks[event], cmd)) {
        s.hooks[event].push({ hooks: [{ type: 'command', command: cmd, timeout: 5000 }] });
        return true;
    }
    return false;
}
const a = addHook(s, 'SessionStart',     activateCmd);
const b = addHook(s, 'UserPromptSubmit', trackerCmd);
fs.writeFileSync(settingsPath, JSON.stringify(s, null, 2) + '\n', 'utf8');
process.stdout.write(
    (a ? 'wired SessionStart -> caveman-activate.js\n' : 'SessionStart already wired\n') +
    (b ? 'wired UserPromptSubmit -> caveman-mode-tracker.js\n' : 'UserPromptSubmit already wired\n')
);
'@

    $tmpJs = [System.IO.Path]::Combine([System.IO.Path]::GetTempPath(), [System.Guid]::NewGuid().ToString() + ".js")
    try {
        [System.IO.File]::WriteAllText($tmpJs, $nodeScript, $utf8NoBom)
        node $tmpJs $settingsPath $activateCmd $trackerCmd | ForEach-Object { Write-Host "hooks  : $_" }
    } finally {
        Remove-Item $tmpJs -ErrorAction SilentlyContinue
    }
}

# --- Run ---

Copy-Dir   "$src\skills"   "$claude\skills"  "skills "
Copy-Files "$src\agents"   "$claude\agents"  "agents "
Copy-Files "$src\hooks"    "$claude\hooks"   "hooks  "

if (Test-Path "$src\commands-ngon") {
    Write-Host "commands-ngon: skipped (NgonENGINE-specific - copy manually if needed)"
}

if (-not $SkipHooks) {
    $resolvedHooks = Resolve-Path "$claude\hooks" -ErrorAction SilentlyContinue
    $hooksAbs = if ($resolvedHooks) { $resolvedHooks.Path } else { "$claude\hooks" }
    Wire-Hooks $hooksAbs "$claude\settings.json"
} else {
    Write-Host "hooks  : skipped settings.json wiring (-SkipHooks)"
}

Write-Host ""
Write-Host "Done. Restart Claude Code to pick up new skills."
Write-Host "Node.js required for caveman hooks."
