Send a message to the Discord channel via webhook.

사용자가 명시적으로 Discord 전송을 요청한 경우에만 사용. Webhook URL은 `.claude/settings.local.json`의 `env.DISCORD_WEBHOOK` (gitignored).

Use the argument as the message content. If no argument is provided, ask the user what to send.

Send the message using the Bash tool with this approach (handles Korean/multiline/special chars):

```bash
cat > /tmp/discord_msg.txt << 'MSGEOF'
<message content here>
MSGEOF
msg=$(cat /tmp/discord_msg.txt)
powershell.exe -ExecutionPolicy Bypass -File "C:\mavis-backend\discord_send.ps1" -Message "$msg"
```

Replace `<message content here>` with the actual message. After sending, confirm to the user that it was sent successfully.

$ARGUMENTS
