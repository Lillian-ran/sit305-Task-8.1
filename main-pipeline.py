from flask import Flask, request, Response
from transformers import pipeline, AutoTokenizer, BitsAndBytesConfig
import torch
import argparse

app = Flask(__name__)
pipe = None
tokenizer = None
# MODEL = "meta-llama/Llama-3.2-1B"
MODEL = "google/gemma-3-1b-it"

def prepareLlamaBot():
    global pipe, tokenizer
    print(f"Loading {MODEL}... This may take a while.")
    # print("Loading Llama-3.2-1B model with 4-bit quantization... This may take a while.")

    # Configure 4-bit quantization
    # quantization_config = BitsAndBytesConfig(
    #     load_in_4bit=True,
    #     bnb_4bit_compute_dtype=torch.float16,
    #     bnb_4bit_quant_type="nf4",
    #     bnb_4bit_use_double_quant=False
    # )

    # Initialize pipeline with quantizatio
    pipe = pipeline(
        "text-generation",
        model=MODEL,
        # device_map="auto", #if gpu
        # torch_dtype=torch.float16 if torch.cuda.is_available() else torch.float32, #if you have GPU
        # model_kwargs={"quantization_config": quantization_config}
    )

    # Load tokenizer for padding
    tokenizer = AutoTokenizer.from_pretrained(MODEL)
    tokenizer.pad_token = tokenizer.eos_token if tokenizer.pad_token is None else tokenizer.pad_token

    print("Model and pipeline loaded successfully.")


@app.route('/')
def index():
    return "Welcome to the Llama Chatbot API!"


@app.route('/chat', methods=['POST'])
def chat():
    global pipe, tokenizer

    # Get userMessage from form data or raw body
    user_message = request.form.get('userMessage') or request.get_data(as_text=True).strip()

    # Validate userMessage
    if not user_message:
        return Response("Error: userMessage cannot be empty", status=400, mimetype='text/plain')

    # Print received request
    print("\nReceived Request:")
    print(f"userMessage: {user_message}")

    # Use raw userMessage as the input
    prompt = user_message

    # Generate response using pipeline
    try:
        outputs = pipe(
            prompt,
            max_new_tokens=100,  # Limit to short responses
            min_new_tokens=1,
            do_sample=True,
            top_p=0.85,  # Focused sampling
            temperature=0.6,  # Low randomness
            pad_token_id=tokenizer.pad_token_id,
            no_repeat_ngram_size=2,  # Prevent repetition
            return_full_text=False
        )
        raw_output = outputs[0]['generated_text'].strip()
    except Exception as e:
        print(f"Error during generation: {str(e)}")
        raw_output = ""

    # Print raw output
    print(f"Raw Model Output: {raw_output}")

    # Use raw output as response
    response = raw_output

    # Fallback for empty, short, or irrelevant responses
    if not response or response.isspace() or len(response.split()) < 3 or len(set(response.split())) < len(
            response.split()) * 0.7:
        response = f"Sorry, I couldn't provide a relevant answer to: '{user_message}'. Please rephrase."

    # Truncate to one sentence if too long
    # response = response.split('.')[0] + '.' if '.' in response else response

    # Print generated response
    print(f"Generated Response: {response}\n")

    # Return plain text response
    return Response(response, mimetype='text/plain')


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--port', type=int, default=5000, help='Specify the port number')
    args = parser.parse_args()

    port_num = args.port
    prepareLlamaBot()
    print(f"App running on port {port_num}")
    app.run(host='0.0.0.0', port=port_num)