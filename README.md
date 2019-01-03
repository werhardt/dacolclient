# dacol-client
Library to collect data and send them via pirg to a configured server.

##### Configuration
```
dacol.url
```

## DacolHostAgent

##### Configuration
The possible configuration of the DacolHostAgent and it's default values.
```
# activate or deactive the DacolHostAgent
dacolhostagent.active=false
# interval when the data is sent to the server in seconds
dacolhostagent.updateinterval=5
# the format of the collected data size (byte, kb, mb, gb)
dacolhostagent.datasize=mb
# the url of the server to send the information.
# if dacolhostagent.url is not configured, dacol.url is used.
dacolhostagent.url=null
```