export interface SagaMessageWrapper<T> {
  operation: null | string;
  data: null | T[];
  correlationId: null;
}
