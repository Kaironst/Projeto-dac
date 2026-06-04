import amqp from "amqplib";
import { rabbitmqUrl } from "../server";
import { randomUUID } from "crypto";
import { UsersDtoCliente } from "../dto/UsersDto";
import { GerentesDtoGerente } from "../dto/GerentesDto";
import { MessageWrapper } from "../dto/MessageWrapper";
import { ContasDtoConta } from "../dto/ContasDto";
import { LoginRequest } from "../dto/LoginRequest";
import { TokenDto } from "../dto/TokenDto";

//diferentemente do spring não temos uma função pré feita para fazer tudo
//(temos que configurar do 0)
class GenericProducerRPC<ReqMessageType, ResMessageType> {

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

    this.connection = await this.connectWithRetry();
    this.channel = await this.connection.createChannel();

    //declara a exchange
    await this.channel.assertExchange(this.exchange, "direct", {});

    //esse é o consumer
    //usa o pseudo_queue reply-to (usado no spring no convertSendAndRecieve)
    await this.channel.consume(
      "amq.rabbitmq.reply-to",
      (msg) => {

        if (!msg) return;

        //pega 
        const correlationId = msg.properties.correlationId;
        const handler = this.pending.get(correlationId);

        if (!handler) return;

        try {
          const parsed = JSON.parse(msg.content.toString()) as ResMessageType;
          handler(parsed);
        } catch (err) {
          console.error("invalid json", err);
        }

        this.pending.delete(correlationId);
      },
      { noAck: true }
    );
  }

  private async connectWithRetry(maxAttempts = 20, delayMs = 1000): Promise<amqp.ChannelModel> {
    let lastError: unknown;

    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        return await amqp.connect(rabbitmqUrl);
      } catch (error) {
        lastError = error;
        console.warn(`RabbitMQ unavailable for ${this.routingKey}. Attempt ${attempt}/${maxAttempts}.`);
        await this.delay(delayMs);
      }
    }

    throw lastError;
  }

  private delay(delayMs: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, delayMs));
  }

  public async requestService(message: ReqMessageType): Promise<ResMessageType> {

    if (!this.channel) {
      throw new Error("canal não inicializado");
    }

    const correlationId = randomUUID();


    const result = await new Promise<ResMessageType>((resolve, reject) => {

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
export const usersProducer = new GenericProducerRPC<MessageWrapper<UsersDtoCliente>, MessageWrapper<UsersDtoCliente>>("app.exchange", "users.key");
export const usersProducerCqrs = new GenericProducerRPC<MessageWrapper<UsersDtoCliente>, MessageWrapper<UsersDtoCliente>>("app.exchange", "cqrs.request.key");
export const gerentesProducer = new GenericProducerRPC<MessageWrapper<GerentesDtoGerente>, MessageWrapper<GerentesDtoGerente>>("app.exchange", "gerentes.key");
export const gerentesProducerCqrs = new GenericProducerRPC<MessageWrapper<GerentesDtoGerente>, MessageWrapper<GerentesDtoGerente>>("app.exchange", "cqrs.request.key");
export const contasProducer = new GenericProducerRPC<MessageWrapper<ContasDtoConta>, MessageWrapper<ContasDtoConta>>("app.exchange", "contas.key");
export const contasProducerCqrs = new GenericProducerRPC<MessageWrapper<ContasDtoConta>, MessageWrapper<ContasDtoConta>>("app.exchange", "cqrs.request.key");
export const authProducer = new GenericProducerRPC<MessageWrapper<LoginRequest>, MessageWrapper<TokenDto>>("app.exchange", "auth.key")
