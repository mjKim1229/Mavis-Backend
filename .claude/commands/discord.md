Send a message to the Discord channel via webhook.

Use the argument as the message content. If no argument is provided, ask the user what to send.

Send the message using this PowerShell script:

```bash
powershell -ExecutionPolicy Bypass -File "C:\mavis-backend\discord_send.ps1" -Message "<message content here>"
```

Replace `<message content here>` with the actual message. After sending, confirm to the user that it was sent successfully.

$ARGUMENTS
