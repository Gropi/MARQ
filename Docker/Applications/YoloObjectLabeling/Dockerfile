FROM ubuntu:18.04

RUN apt-get update
RUN apt-get -y install python3.8

RUN export PYTHONPATH=/var/lib/python/

RUN apt-get -y install python3-pip

# This is not working for some reasons... 
RUN alias python=python3.8

RUN python3.8 -m pip install --upgrade pip
	
# We copy just the requirements.txt first to leverage Docker cache
COPY ./build/requirements.txt /app/requirements.txt

WORKDIR /app

RUN pip install -r requirements.txt

COPY ./build/main.py /app

ENTRYPOINT [ "python3.8" ]

CMD [ "main.py" ]