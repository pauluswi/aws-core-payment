#!/usr/bin/env python3
from flask import Flask, request, jsonify
from datetime import datetime

app = Flask(__name__)
store = []

@app.route('/publish', methods=['POST'])
def publish():
    try:
        body = request.get_json(force=True)
        topic = body.get('topic')
        key = body.get('key')
        payload = body.get('payload')
        entry = f"{datetime.utcnow().isoformat()}|{topic}|{key}|{payload}"
        store.append(entry)
        return jsonify({'status':'ok'}), 200
    except Exception as e:
        return jsonify({'status':'error','error':str(e)}), 400

@app.route('/events', methods=['GET'])
def events():
    return "\n".join(store), 200

@app.route('/health', methods=['GET'])
def health():
    return jsonify({'status':'UP'}), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8081)
