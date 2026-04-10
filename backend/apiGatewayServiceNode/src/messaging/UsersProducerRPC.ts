import amqp from "amqplib";
import { UsersDtoCliente, UsersDtoMessage } from "../dto/UsersDto";

//diferentemente do spring não temos uma função pré feita para fazer tudo
//(temos que configurar do 0)
class UsersProducerRPC {
  private ORCHESTRATOR_KEY = "orchestrator.key"
  private APP_EXCHANGE = "app.exchange";

  private connection: amqp.ChannelModel | null = null;
  private channel: amqp.Channel | null = null;
  private pending = new Map<string, (msg: UsersDtoMessage) => void>();

  private generateUUID(): string {
    return Math.random().toString() +
      Math.random().toString() +
      Math.random().toString();
  }

  //inicializa a conexão com o rabbitmq e o consumer
  async init() {
    if (this.connection !== null && this.channel !== null) return;
    this.connection = await amqp.connect('amqp://usuario:admin@localhost');
    this.channel = await this.connection.createChannel();

    await this.channel.assertExchange(this.APP_EXCHANGE, "direct", {});

    //usa o pseudo_queue reply-to (usado no spring no convertSendAndRecieve)
    await this.channel.consume(
      "amq.rabbitmq.reply-to",
      (msg) => {
        if (!msg) return;

        const correlationId = msg.properties.correlationId;
        const handler = this.pending.get(correlationId);

        if (handler) {
          try {
            const parsed = JSON.parse(msg.content.toString()) as UsersDtoMessage;
            handler(parsed);
          } catch (err) {
            console.error("invalid json")
          }

          this.pending.delete(correlationId);
        }
      },
      { noAck: true }
    );
  }

  public async requestOrchestratorService(operation: string, data: UsersDtoCliente | null): Promise<UsersDtoMessage> {

    const correlationId = this.generateUUID();
    const message: UsersDtoMessage = { operation, data };

    const result = await new Promise<UsersDtoMessage>((resolve, reject) => {

      const timeout = setTimeout(() => {
        this.pending.delete(correlationId);
        reject(new Error("timed out"));
      }, 5000)

      this.pending.set(correlationId, (response) => {
        clearTimeout(timeout);
        resolve(response);
      })

      this.channel!.publish(
        this.APP_EXCHANGE,
        this.ORCHESTRATOR_KEY,
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

export const usersProducerRpc = new UsersProducerRPC();
