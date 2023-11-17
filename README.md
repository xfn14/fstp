# Fast Sender Transfer Protocol (FSTP) 

Partilha de ficheiros entre ___peers___ com um servidor central para gerir e manter tais ficheiros.

## Protocolo

São então desenvolvidos dois protocolos: FS Track Protocol, para rastrear os arquivos e FS Transfer Protocol para fazer as transferências de ficheiros.

### FS Transfer Protocol (UDP)

Tamanho do pacote: 4096

- __1 byte__ - packet id
- __[...]__ - payload

#### Payload

- 1 - Request file chunk
    - __2 + str len + str bytes__ - file_path (string)
    - __8 bytes__ -  chunk id (long)

- 10 - File chunk (Response to code 1)
    - __8 bytes__ -  chunk id (long)
    - __2 bytes__ - tamanho da data
    - __[tamanho da data]__ - data

- 40 - Invalid chunk request (Response to code 1)

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

- 3 - Get file
    - __2 + str len + str bytes__ - file_path (string)
    - __8 bytes__ - last modified (long)
    - __4 bytes__ - number of connected peers (int)
        - *per peer connected*
        - __2 + str len + str bytes__ -  peer ip (string)

- 10 - List of nodes connected (Response to code 0)

- 11 - No nodes connected (Response to code 0)

- 12 - File registered (Response to code 1)

- 13 - File blocks (Response to code 3)
    - __4 bytes__ - number of chunks (int)
        - *per chunk*
        - __8 bytes__ -  chunk id (long)

- 20 - List of files and their peers to update (Response to code 2)
    - __4 bytes__ - number of files to update (int)
        - *per file to update*
        - __2 + str len + str bytes__ - file_path_1 (string)
        - __8 bytes__ - last modified (long)
        - __4 bytes__ - number of peers (int)
            - *per peer*
            - __2 + str len + str bytes__ -  peer address (long)

- 21 - No files to update (Response to code 2)

- 40 - Failed to register file (Response to code 1)

- 41 - Error starting file download (Response to code 3)

## Implementação

### Pros

- Packet pequenos fazem com que a retransmissão no que toda a perdas seja mais eficaz.
- Através do uso de checksums para os ids das chunks conseguimos, mesmo usando UDP, ter a certeza que a informação não é alterada durante a transmissão.
- O tracker nao tem de guardar os ficheiros por inteiro para saber verificar as suas chunks.
- O cliente pode ter multiplos peers ao mesmo tempo a mandar o ficheiro e ao mesmo tempo enviar as partes que já fez download para outros clientes que também precisem do mesmo ficheiro.

### Contras

- Packets UDP que nao vao completos porque o ficheiro não tem mais que Constant.UDP_BUFFER_SIZE ou a ultima chunk de um ficheiro tem bytes nao usados.
- Packet pequenos faz com que sejam usados mais bytes em headers na transferencia de ficheiros maiores.
- Não ser possível fazer download de vários ficheiros ao mesmo tempo
- Ficheiro de tamanho maximo de 2Gb