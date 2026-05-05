import Groq from "groq-sdk";

// Khởi tạo client Groq
const groq = new Groq({
  apiKey: process.env.GROQ_API_KEY || "",
});

// Chuyển System Instruction ra thành một hằng số
const SYSTEM_INSTRUCTION = `
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
`;

export class ChatLLMUtils {
  public static async processChatMessage(message: string, history: any[] = []) {
    try {
      // 1. Khởi tạo mảng tin nhắn, luôn bắt đầu bằng chỉ thị hệ thống (System Prompt)
      const messages: any[] = [{ role: "system", content: SYSTEM_INSTRUCTION }];

      // 2. Format lại lịch sử chat (nếu có)
      // Groq dùng role "assistant" thay vì "model" như Gemini
      const formattedHistory = history.map((msg) => ({
        role: msg.role === "user" ? "user" : "assistant",
        content: msg.text, // Điều chỉnh msg.text cho khớp với cấu trúc history thực tế của bạn
      }));

      messages.push(...formattedHistory);

      // 3. Thêm câu hỏi hiện tại của người dùng vào cuối mảng
      messages.push({ role: "user", content: message });

      // 4. Gọi API của Groq
      const chatCompletion = await groq.chat.completions.create({
        messages: messages,
        // Sử dụng Llama 3 70B: Rất thông minh, đa ngôn ngữ tốt và cực kỳ nhanh
        model: "llama-3.3-70b-versatile",
        max_tokens: 1000,
        temperature: 0.7, // Nhiệt độ 0.7 giúp câu trả lời tự nhiên, sáng tạo nhưng vẫn bám sát chủ đề
      });

      // Trả về câu trả lời từ Groq
      return chatCompletion.choices[0]?.message?.content || "";
    } catch (error) {
      console.error("Error calling Groq API:", error);
      throw new Error(
        "Failed to process chat message. Please try again later.",
      );
    }
  }
}
