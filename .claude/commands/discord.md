Send a message to the Discord channel via webhook.

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
