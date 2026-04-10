"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var express_1 = require("express");
var ClienteController_1 = require("./controller/ClienteController");
var UsersProducerRPC_1 = require("./messaging/UsersProducerRPC");
var app = (0, express_1.default)();
var port = process.env.PORT || 3000;
app.get("/controller/ClienteController", ClienteController_1.default);
UsersProducerRPC_1.usersProducerRpc.init();
app.listen(port, function () {
    console.log("[server]: Server is running at http://localhost:".concat(port));
});
