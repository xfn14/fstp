# Fast Sender Transfer Protocol (FSTP) 

Partilha de ficheiros entre ___peers___ com um servidor central para gerir e manter tais ficheiros.

## Protocolo

São então desenvolvidos dois protocolos: FS Track Protocol, para rastrear os arquivos e FS Transfer Protocol para fazer as transferências de ficheiros.

### FS Transfer Protocol (UDP)

Tamanho do pacote: 4096

- __1 byte__ - packet id
- __2 bytes__ - tamanho do payload
- __[...]__ - payload

### FS Track Protocol (TCP)

- __1 byte__ - tipo de mensagem
- __4 bytes__ - tamanho do payload
- __[...]__ - payload

#### Tipos de mensagens

##### Node 

- 1 - Register File
    - __2 + str len + str bytes__ - file_path*file_lastmodified (string)
    - __4 bytes__ - number of chunks (int)
    - __8 bytes__ - *per chunk* chunk id (long)

Response:
    - 10 - File registered
    - 40 - Failed to register file

- 11 - GET Request
    - Payload: <file1_name>,<file2_name>,...
- 20 - Request list of files versions
    - Payload: LIST
- 40 - Close connection
    - Payload: Bye world!

##### Tracker

- 10 - Response to tracker ping.
    - Payload: Pong!
- 11 - GET Reponse OK
    - Payload: <peer1_address>,<peer2_address>,...
- 20 - Send list of files versions
    - Payload: <file1_path>*<file1_checksum>*<file1_lastModified>^<peer1_address>~<peer2_address>~...,<file2_path>*<file2_checksum>*<file2_lastModified>^<peer2_address>~<peer3_address>~...,...
- 40 - Close connection
    - Payload: Goodbye.
- 41 - GET Response Fail
    - Payload: <file1_failed>,<file2_failed>,...
