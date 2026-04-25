param([string]$Message)
$url = $env:DISCORD_WEBHOOK
$body = @{ content = $Message } | ConvertTo-Json
$bytes = [System.Text.Encoding]::UTF8.GetBytes($body)
Invoke-RestMethod -Uri $url -Method Post -ContentType 'application/json; charset=utf-8' -Body $bytes
