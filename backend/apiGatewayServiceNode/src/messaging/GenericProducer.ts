
import amqp from "amqplib";
import { rabbitmqUrl } from "../server";
import { SagaMessageWrapper } from "../dto/SagaMessageWrapper";

//diferentemente do spring não temos uma função pré feita para fazer tudo
//(temos que configurar do 0)
class GenericProducer<MessageType> {

  private connection: amqp.ChannelModel | null = null;
  private channel: amqp.Channel | null = null;

  constructor(
    private exchange: string,
    private routingKey: string,
  ) { }

  //inicializa a conexão com o rabbitmq e o consumer
  async init() {
    if (this.connection && this.channel) return;

    this.connection = await amqp.connect(rabbitmqUrl);
    this.channel = await this.connection.createChannel();

    await this.channel.assertExchange(this.exchange, "direct", {});
  }

  public async messageService(message: MessageType) {

    if (!this.channel) {
      throw new Error("canal não inicializado");
    }

    this.channel!.publish(
      this.exchange,
      this.routingKey,
      Buffer.from(JSON.stringify(message)),
      {
        contentType: "application/json"
      });

  }

}


export default GenericProducer;
export const sagaProducer = new GenericProducer<SagaMessageWrapper<any>>("app.exchange", "orchestrator.saga.key");
