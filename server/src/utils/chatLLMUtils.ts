import { GoogleGenerativeAI } from "@google/generative-ai";

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY || "");

const chatModel = genAI.getGenerativeModel({
  model: "gemini-3.1-flash-lite-preview",
  systemInstruction: `
    Bạn là một trợ lý du lịch ảo thông minh và thân thiện của ứng dụng du lịch "TourGuide".
    Nhiệm vụ của bạn là:
    1. Gợi ý các địa điểm du lịch, nhà hàng, khách sạn nổi bật.
    2. Cung cấp thông tin chi tiết về các địa điểm hoặc lên lịch trình chuyến đi.
    3. Tóm tắt thông tin du lịch một cách ngắn gọn, dễ hiểu.
    
    Quy tắc TỐI QUAN TRỌNG: 
    - ĐA NGÔN NGỮ (Multi-language): Hãy tự động nhận diện ngôn ngữ trong tin nhắn của người dùng. Nếu người dùng hỏi bằng Tiếng Việt, hãy trả lời bằng Tiếng Việt. Nếu người dùng hỏi bằng Tiếng Anh, BẮT BUỘC phải trả lời bằng Tiếng Anh.
    - Chỉ trả lời các câu hỏi liên quan đến DU LỊCH, địa điểm, văn hóa, ẩm thực, và di chuyển. 
    - Nếu người dùng hỏi các chủ đề khác (toán học, lập trình, chính trị, v.v.), hãy lịch sự từ chối và hướng họ quay lại chủ đề du lịch.
    - Luôn giữ thái độ thân thiện và có sử dụng emoji để thêm phần sinh động.
  `,
});

export class ChatLLMUtils {
  public static async processChatMessage(message: string, history: any[] = []) {
    try {
      const formattedHistory = history.map((msg) => ({
        role: msg.role === "user" ? "user" : "model",
        parts: [{ text: msg.text }],
      }));

      const chat = chatModel.startChat({
        history: formattedHistory,
        generationConfig: {
          maxOutputTokens: 1000,
        },
      });

      const result = await chat.sendMessage(message);
      return result.response.text();
    } catch (error) {
      console.error("Error calling ChatBot AI:", error);
      throw new Error(
        "Failed to process chat message. Please try again later.",
      );
    }
  }
}
