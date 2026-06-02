package br.ufpr.dac.cqrsService.model;

import tools.jackson.databind.JsonNode;

public interface DebeziumModel {

  public void handleUpsert(JsonNode data);

  public void handleDelete(JsonNode data);

}
