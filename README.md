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

#### Payload

- 0 - Ping

- 1 - Register File
    - __2 + str len + str bytes__ - file_path (string)
    - __8 bytes__ - last modified (long)
    - __4 bytes__ - number of chunks (int)
        - *per chunk*
        - __8 bytes__ -  chunk id (long)

- 2 - Get update list

- 10 - List of nodes connected (Response to code 0)

- 11 - No nodes connected (Response to code 0)

- 12 - File registered (Response to code 1)

- 20 - List of files and their peers to update
    - __4 bytes__ - number of files to update (int)
        - *per file to update*
        - __2 + str len + str bytes__ - file_path_1 (string)
        - __8 bytes__ - last modified (long)
        - __4 bytes__ - number of peers (int)
            - *per peer*
            - __2 + str len + str bytes__ -  peer address (long)

- 21 - No files to update

- 40 - Failed to register file (Response to code 1)
