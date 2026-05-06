import amqp from "amqplib";
import { rabbitmqUrl } from "../server";
import { randomUUID } from "crypto";
import { UsersDtoCliente } from "../dto/UsersDto";
import { GerentesDtoGerente } from "../dto/GerentesDto";
import { MessageWrapper } from "../dto/MessageWrapper";

//diferentemente do spring não temos uma função pré feita para fazer tudo
//(temos que configurar do 0)
class GenericProducerRPC<MessageType> {

  private connection: amqp.ChannelModel | null = null;
  private channel: amqp.Channel | null = null;
  private pending = new Map<string, (msg: any) => void>();

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

    //usa o pseudo_queue reply-to (usado no spring no convertSendAndRecieve)
    await this.channel.consume(
      "amq.rabbitmq.reply-to",
      (msg) => {

        if (!msg) return;

        const correlationId = msg.properties.correlationId;
        const handler = this.pending.get(correlationId);

        if (!handler) return;

        try {
          const parsed = JSON.parse(msg.content.toString()) as MessageType;
          handler(parsed);
        } catch (err) {
          console.error("invalid json", err);
        }

        this.pending.delete(correlationId);
      },
      { noAck: true }
    );
  }

  public async requestService(message: MessageType): Promise<MessageType> {

    if (!this.channel) {
      throw new Error("canal não inicializado");
    }

    const correlationId = randomUUID();


    const result = await new Promise<MessageType>((resolve, reject) => {

      const timeout = setTimeout(() => {
        this.pending.delete(correlationId);
        reject(new Error("timed out"));
      }, 5000)

      this.pending.set(correlationId, (response) => {
        clearTimeout(timeout);
        resolve(response);
      })

      this.channel!.publish(
        this.exchange,
        this.routingKey,
        Buffer.from(JSON.stringify(message)),
        {
          correlationId: correlationId,
          replyTo: "amq.rabbitmq.reply-to",
          contentType: "application/json"
        });

    });

    console.log(`got result ${result}`);
    return result;
  }
}

export default GenericProducerRPC;
export const usersProducer = new GenericProducerRPC<MessageWrapper<UsersDtoCliente>>("app.exchange", "orchestrator.users.key");
export const gerentesProducer = new GenericProducerRPC<MessageWrapper<GerentesDtoGerente>>("app.exchange", "orchestrator.gerentes.key");
