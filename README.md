# SOFT-IoT-DLT-Client-Tangle-Hornet

O `soft-iot-dlt-client-tangle-hornet` é o _bundle_ responsável por provê a serviços de escrita e leitura na rede [*Tangle Hornet*](https://wiki.iota.org/hornet/getting_started/). Ele também especifica um conjunto de transações que são utilizados no protocolo de balanceamento de carga e no sistema de reputação distribuído.

## Configurações

| Propriedade | Descrição | Valor Padrão |
| ----------- | --------- | ------------ |
| DLT_PROTOCOL | Define qual é o tipo de protocolo utilizado pelo cliente da API. | http |
| DLT_URL | Define qual é a URL do nó da rede que o cliente deve se conectar. | 127.0.0.1 |
| DLT_PORT | 	Define a porta. | 3000 |
| BUFFER_MAX_LEN | Define o tamanho máximo do `buffer` que armazena as transações que serão enviadas para a rede. | 128 |
| ZMQ_SOCKET_PROTOCOL | Define qual é o protocolo do sistema de mensageria que utilizado pela rede para notificar o estado das transações. | tcp |
| ZMQ_SOCKET_URL | Define qual é a URL do sistema de mensageria que utilizado pela rede para notificar o estado das transações. | 127.0.0.1 |
| ZMQ_SOCKET_PORT | Define qual é a porta do sistema de mensageria que utilizado pela rede para notificar o estado das transações. | 5556
| debugModeValue | Define se serão exibidas mensagens de depuração | true |
