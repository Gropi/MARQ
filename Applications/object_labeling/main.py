import random

from flask import Flask, redirect, url_for, request
import cv2
import numpy as np
import torch
import time
import base64
import json

app = Flask(__name__)

torch.cuda.is_available()
torch.cuda.empty_cache()
modelSmall = torch.hub.load('ultralytics/yolov5', 'yolov5s')
modelMedium = torch.hub.load('ultralytics/yolov5', 'yolov5m')
modelLarge = torch.hub.load('ultralytics/yolov5', 'yolov5l')
modelX = torch.hub.load('ultralytics/yolov5', 'yolov5x')

@app.route("/yolov/unloaded", methods = ["POST"])
def findObjectsWithoutLoadedYolov():
    jsonString = json.loads(request.data)

    variant = jsonString['type']
    decoded_img = base64.b64decode(jsonString['image'])

    if variant == 's':
        usedModel = torch.hub.load('ultralytics/yolov5', 'yolov5s')
    elif variant == 'm':
        usedModel = torch.hub.load('ultralytics/yolov5', 'yolov5m')
    elif variant == 'l':
        usedModel = torch.hub.load('ultralytics/yolov5', 'yolov5l')
    elif variant == 'x':
        usedModel = torch.hub.load('ultralytics/yolov5', 'yolov5x')
    else:
        return
    return findObjects(usedModel, decoded_img)


@app.route("/yolov/loaded", methods = ["POST"])
def findObjectsWithLoadedYolov():
    jsonString = json.loads(request.data)

    variant = jsonString['type']
    decoded_img = base64.b64decode(jsonString['image'])

    if variant == 's':
        usedModel = modelSmall
    elif variant == 'm':
        usedModel = modelMedium
    elif variant == 'l':
        usedModel = modelLarge
    elif variant == 'x':
        usedModel = modelX
    else:
        return
    return findObjects(usedModel, decoded_img)


def findObjects(usedModel, decoded_img):
    start_time = time.time()
    frame = cv2.imdecode(np.frombuffer(decoded_img, dtype=np.uint8), 1)
    image = cv2.cvtColor(frame, cv2.COLOR_RGBA2BGRA)

    results = usedModel(image)

    # If you want to see the result
    r_img = results.render()  # returns a list with the images as np.array
    img_with_boxes = r_img[0]  # image with boxes as np.array
    cv2.imshow('received image', img_with_boxes);
    cv2.waitKey()

    response_objects = []
    for result in results.xywh:
        for detection in result.T.transpose(0,1).tolist():
            x, y, w, h, confidence, i = detection
            label = results.names[int(i)]
            response_objects.append({
                'x':x,
                'y':y,
                'w':w,
                'h':h,
                'confidence':confidence,
                'label':label
                })
        else: #no object detected
            pass

        print(response_objects)

        time.sleep(random.random()/10)

        end_time = time.time()

        response = {
            'objects': response_objects,
            'computation_time_ms': 1000*(end_time-start_time)
        }
    return response


if __name__ == '__main__':
    app.run(debug = True, host = "0.0.0.0")

