from flask import Flask, request, jsonify
import google.generativeai as genai
import os

app = Flask(__name__)

# 🔴 YOUR API KEY
GEMINI_API_KEY = "AIzaSyCUPQyVA_ylED1jLlA7eOeZFK6PXuntomw"

# Configure Gemini
model = None
try:
    genai.configure(api_key=GEMINI_API_KEY)
    
    # 🔍 Auto-detect available models
    print("Checking available models...")
    available_models = []
    for m in genai.list_models():
        if 'generateContent' in m.supported_generation_methods:
            available_models.append(m.name)
    
    print(f"Found models: {available_models}")

    # Pick the best available one
    selected_model_name = "gemini-1.5-flash" # default preference
    
    if "models/gemini-1.5-flash" in available_models:
        selected_model_name = "gemini-1.5-flash"
    elif "models/gemini-pro" in available_models:
        selected_model_name = "gemini-pro"
    elif len(available_models) > 0:
        # Fallback to the first available one (e.g., gemini-1.0-pro)
        selected_model_name = available_models[0].replace("models/", "")
    
    print(f"✅ USING MODEL: {selected_model_name}")
    model = genai.GenerativeModel(selected_model_name)
    print("✅ SERVER READY!")

except Exception as e:
    print(f"❌ CONFIG ERROR: {e}")

@app.route('/chat', methods=['POST'])
def chat():
    try:
        # check if model loaded
        if not model:
            return jsonify({"reply": "System Error: Gemini model failed to load. Check terminal for details."}), 200

        data = request.json
        user_message = data.get('message', '')

        if not user_message:
            return jsonify({"reply": "I'm listening..."}), 200

        # Generate response
        response = model.generate_content(
            f"Act as a mental health assistant named Echo. Be kind and short. User: {user_message}"
        )
        
        # Send back text
        return jsonify({"reply": response.text.replace('*', '')}), 200

    except Exception as e:
        print(f"RUNTIME ERROR: {e}")
        # RETURN 200 so the phone displays the error
        return jsonify({"reply": f"System Error: {str(e)}"}), 200

if __name__ == '__main__':
    print("starting server on port 5000...")
    app.run(host='0.0.0.0', port=5000)
