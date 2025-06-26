import axios from 'axios';
import { API_URL } from '../../config';

const client = axios.create({
    // baseURL: 'localhost:4000/',
    baseURL: `${API_URL}`,
    // baseURL: 'http://api-server.com/,

});

client.interceptors.request.use(
    (config) => {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers.Authorization = `${token}`;
      }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );

export default client;
