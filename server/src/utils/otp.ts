export const EMAIL_TOKEN_TTL = 5 * 60 * 1000;

export const generateOTP = () => {
  return Math.floor(100000 + Math.random() * 900000).toString();
};