"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (g && (g = 0, op[0] && (_ = 0)), _) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.usersProducerRpc = void 0;
var amqplib_1 = require("amqplib");
//diferentemente do spring não temos uma função pré feita para fazer tudo
//(temos que configurar do 0)
var UsersProducerRPC = /** @class */ (function () {
    function UsersProducerRPC() {
        this.ORCHESTRATOR_KEY = "orchestrator.key";
        this.APP_EXCHANGE = "app.exchange";
        this.connection = null;
        this.channel = null;
        this.pending = new Map();
    }
    UsersProducerRPC.prototype.generateUUID = function () {
        return Math.random().toString() +
            Math.random().toString() +
            Math.random().toString();
    };
    //inicializa a conexão com o rabbitmq e o consumer
    UsersProducerRPC.prototype.init = function () {
        return __awaiter(this, void 0, void 0, function () {
            var _a, _b;
            var _this = this;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0:
                        if (this.connection !== null && this.channel !== null)
                            return [2 /*return*/];
                        _a = this;
                        return [4 /*yield*/, amqplib_1.default.connect('amqp://usuario:admin@localhost')];
                    case 1:
                        _a.connection = _c.sent();
                        _b = this;
                        return [4 /*yield*/, this.connection.createChannel()];
                    case 2:
                        _b.channel = _c.sent();
                        return [4 /*yield*/, this.channel.assertExchange(this.APP_EXCHANGE, "direct", {})];
                    case 3:
                        _c.sent();
                        //usa o pseudo_queue reply-to (usado no spring no convertSendAndRecieve)
                        return [4 /*yield*/, this.channel.consume("amq.rabbitmq.reply-to", function (msg) {
                                if (!msg)
                                    return;
                                var correlationId = msg.properties.correlationId;
                                var handler = _this.pending.get(correlationId);
                                if (handler) {
                                    try {
                                        var parsed = JSON.parse(msg.content.toString());
                                        handler(parsed);
                                    }
                                    catch (err) {
                                        console.error("invalid json");
                                    }
                                    _this.pending.delete(correlationId);
                                }
                            }, { noAck: true })];
                    case 4:
                        //usa o pseudo_queue reply-to (usado no spring no convertSendAndRecieve)
                        _c.sent();
                        return [2 /*return*/];
                }
            });
        });
    };
    UsersProducerRPC.prototype.requestOrchestratorService = function (operation, data) {
        return __awaiter(this, void 0, void 0, function () {
            var correlationId, message, result;
            var _this = this;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0:
                        correlationId = this.generateUUID();
                        message = { operation: operation, data: data };
                        return [4 /*yield*/, new Promise(function (resolve, reject) {
                                var timeout = setTimeout(function () {
                                    _this.pending.delete(correlationId);
                                    reject(new Error("timed out"));
                                }, 5000);
                                _this.pending.set(correlationId, function (response) {
                                    clearTimeout(timeout);
                                    resolve(response);
                                });
                                _this.channel.publish(_this.APP_EXCHANGE, _this.ORCHESTRATOR_KEY, Buffer.from(JSON.stringify(message)), {
                                    correlationId: correlationId,
                                    replyTo: "amq.rabbitmq.reply-to",
                                    contentType: "application/json"
                                });
                            })];
                    case 1:
                        result = _a.sent();
                        console.log("got result ".concat(result));
                        return [2 /*return*/, result];
                }
            });
        });
    };
    return UsersProducerRPC;
}());
exports.usersProducerRpc = new UsersProducerRPC();
