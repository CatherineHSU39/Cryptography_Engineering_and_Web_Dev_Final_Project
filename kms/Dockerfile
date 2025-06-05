# kms/Dockerfile

FROM python:3.10-slim

# Set working directory
WORKDIR /app

# Install git (required for installing Git-based dependencies)
RUN apt-get update && apt-get install -y git && rm -rf /var/lib/apt/lists/*

# Install dependencies
COPY ./requirements.txt /app/requirements.txt
RUN pip install --no-cache-dir -r /app/requirements.txt

# Copy the application
COPY . /app

# Expose API port
EXPOSE 8000

# Run FastAPI app with hot reload for dev
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
