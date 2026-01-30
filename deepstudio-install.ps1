# ===============================
# DeepStudio bootstrap installer
# ===============================

$ErrorActionPreference = "Stop"

$PackageName = if ($env:DEEPSTUDIO_PKG) { $env:DEEPSTUDIO_PKG } else { "deepstudio-server" }
$Registry = if ($env:DEEPSTUDIO_REGISTRY) { $env:DEEPSTUDIO_REGISTRY } else { "https://microsoft.pkgs.visualstudio.com/OS/_packaging/DeepStudio/npm/registry/" }
$AuthPrefix = "//microsoft.pkgs.visualstudio.com/OS/_packaging/DeepStudio/npm/registry/"

function Require-Npm {
  if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
    Write-Host ""
    Write-Host "❌ Node.js / npm not found."
    Write-Host ""
    Write-Host "Please install Node.js first:"
    Write-Host "https://nodejs.org/en/download"
    Write-Host ""
    exit 1
  }
}

function Read-PatSecure {
  $secure = Read-Host "Enter Azure DevOps PAT (Packaging:Read)" -AsSecureString
  $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
  try {
    return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
  }
  finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
  }
}

function SetCfg($k, $v) { npm config set --global $k $v | Out-Null }
function DelCfg($k) { try { npm config delete --global $k | Out-Null } catch {} }

Require-Npm

$pat = Read-PatSecure
if ([string]::IsNullOrWhiteSpace($pat)) { throw "PAT is empty." }

$patB64 = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($pat))
$pat = $null

try {
  Write-Host "Installing $PackageName@latest ..."

  SetCfg "registry" $Registry
  SetCfg "${AuthPrefix}:username" "microsoft"
  SetCfg "${AuthPrefix}:_password" $patB64
  SetCfg "${AuthPrefix}:email" "npm@example.com"

  npm install -g "$PackageName@latest" --registry $Registry

  Write-Host ""
  Write-Host "✅ DeepStudio installed or upgraded successfully."
}
finally {
  DelCfg "registry"
  DelCfg "${AuthPrefix}:username"
  DelCfg "${AuthPrefix}:_password"
  DelCfg "${AuthPrefix}:email"
}
