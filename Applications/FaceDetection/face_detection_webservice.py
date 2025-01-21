import face_recognition
from flask import Flask, jsonify, request, redirect
import numpy as np

# You can change this to any folder on your system
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif'}

app = Flask(__name__)


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


@app.route('/faces/<detector>', methods=['POST'])
def upload_image(detector):
    if detector != "hog" and detector != "cnn":
        return "Invalid detector. Please select valid detector as endpoint."

    if request.method == 'POST':
        if 'file' not in request.files:
            return "Request invalid. Please add 'file'."

        file = request.files['file']

        if file.filename == '':
            return "Request invalid. Please provide .png, .jpg, .jpeg or .gif-File."

        if file and allowed_file(file.filename):
            return detect_faces_in_image(file, detector)

        return "Request invalid. Please provide .png, .jpg, .jpeg or .gif-File."
    # If no valid image file was uploaded, show the file upload form:
    return "Request invalid."


def detect_faces_in_image(file_stream, detector):
    img = face_recognition.load_image_file(file_stream)

    face_locations = face_recognition.face_locations(img, 1, detector)

    result = []

    for (top, right, bottom, left) in face_locations:
        result.append({"bbox": (left, top, right, bottom), "class_name": "face"})

    return jsonify(result)

    # Get face encodings for any faces in the uploaded image
    unknown_face_encodings = face_recognition.face_encodings(img)

    if len(unknown_face_encodings) > 0:
        # See if the first face in the uploaded image matches the known face of Obama
        match_results = face_recognition.compare_faces([known_face_encoding], unknown_face_encodings[0])
        if match_results[0]:
            is_obama = True

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8080, debug=True)
