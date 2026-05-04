import express from 'express';
import { handleSendEmail } from './controller/emailController';

const app = express();
app.use(express.json());

const port = Number(process.env.PORT || 3005);

app.post('/send-email', handleSendEmail);

app.listen(port, () => {
  console.log(`Email service running on port ${port}`);
});