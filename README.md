

## Rocket TOPIC

```shell

./bin/mqadmin updateTopic -c DefaultCluster -t SEND_MESSAGE_TO_TOPIC -n 127.0.0.1:9876
./bin/mqadmin updateTopic -c DefaultCluster -t CHAT_CHANNEL_MESSAGE_HANDLE_TOPIC -n 127.0.0.1:9876
./bin/mqadmin updateTopic -c DefaultCluster -t PUSH_MESSAGE_TOPIC -n 127.0.0.1:9876
./bin/mqadmin updateTopic -c DefaultCluster -t NOTIFY_MESSAGE_TOPIC -n 127.0.0.1:9876
./bin/mqadmin updateTopic -c DefaultCluster -t DYNAMIC_TIMELINE_HANDLE_TOPIC -o true -n 127.0.0.1:9876 -a +message.type=FIFO
```