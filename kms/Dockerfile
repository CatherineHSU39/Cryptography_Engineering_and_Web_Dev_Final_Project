# kms/Dockerfile

FROM python:3.10-slim

# Set working directory
WORKDIR /code

# Install dependencies
COPY ./requirements.txt /code/requirements.txt
RUN pip install --no-cache-dir -r /code/requirements.txt

# Copy the application code
COPY . /code

# Expose API port
EXPOSE 8000

# Run FastAPI app with hot reload for dev
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]