# Fast Sender Transfer Protocol (FSTP) 

Partilha de ficheiros entre ___peers___ com um servidor central para gerir e manter tais ficheiros.

## Protocolo

São então desenvolvidos dois protocolos: FS Track Protocol, para rastrear os arquivos e FS Transfer Protocol para fazer as transferências de ficheiros.

### FS Transfer Protocol (UDP)

Tamanho do pacote: 4096

- __1 byte__ - packet id
- __2 bytes__ - espaço usado do pacote
- __[...]__ - payload

### FS Track Protocol (TCP)

- __1 byte__ - tipo de mensagem
- __4 bytes__ - tamanho do payload
- __[...]__ - payload

#### Tipos de mensagens

- 10 - Pedir listagem de ficheiros
    - Payload: "Hello world!"
- 11 - GET Request
    - Payload: <file1_name>,<file2_name>,...
- 21 - Ficheiro(s) encontrado(s)
    - Payload: <file1_path>_<file1_block1_id>_<file1_block2_id>_...,<file2_path>_<file2_block1_id>_<file2_block2_id>_...,...
- 20 - Listagem de ficheiros
    - Payload: <file1_path>_<file1_size>_<file1_last_modified_date>,<file2_path>_<file2_size>_<file2_last_modified_date>,...
- 40 - Ficheiro(s) pedido(s) não encontrado(s)
    - Payload: <file1_name>,<file2_name>,...