import express from 'express';
import { handleSendEmail } from './controller/emailController';

const app = express();
app.use(express.json());

app.post('/send-email', handleSendEmail);

app.listen(3005, () => {
  console.log('Email service running on port 3005');
});