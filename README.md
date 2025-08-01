# 🎬 Spring AI Video Analysis: Transcribe, Summarize & Query Video with LLMs

This repository demonstrates how to build a comprehensive multimodal AI video analysis API using Spring AI and Google Gemini. The application can process video from multiple sources (file uploads, URLs, Base64, classpath) and provide intelligent insights by combining text prompts with video analysis. This project showcases the power of multimodal AI that processes both text and video simultaneously, enabling scene description, object recognition, transcription, and content summarization.

📖 **Dive Deeper**: For a complete walkthrough, detailed explanations of multimodal AI concepts, and step-by-step instructions for building this comprehensive video analysis service, read our in-depth tutorial.<br>
👉 [Spring AI Video Analysis: Transcribe, Summarize & Query Video with LLMs](https://bootcamptoprod.com/spring-ai-video-analysis-guide/)

🎥 **Visual Learning**: Prefer video tutorials? Watch our step-by-step implementation guide on YouTube.<br>
👉 YouTube Tutorial - Coming soon!

---

## 📦 Environment Variables

Make sure to provide this Java environment variable when running the application:

-   `GEMINI_API_KEY`: Your Google Gemini API key.

---

## 💡 About This Project

This project implements a **comprehensive Video Analysis API** to demonstrate the power of multimodal AI with Spring AI. It showcases how to:

*   Process video from 4 different input sources: classpath resources, file uploads, web URLs, and Base64 strings.
*   Configure Spring AI to work with Google's Gemini models through OpenAI-compatible endpoints.
*   Build a robust REST API with proper error handling, validation, and support for large file uploads.
*   Combine text prompts with video content for intelligent, context-aware responses.

The application exposes four REST endpoints that accept video files in different formats along with text prompts, sends them to Gemini for multimodal analysis, and returns AI-generated insights about the video content, including scene descriptions, object recognition, and content summaries.

---