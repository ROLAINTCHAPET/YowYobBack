FROM python:3.11-slim

WORKDIR /app

# On retire l'étape apt-get qui échoue à cause d'un problème réseau Docker passager.
# sentence-transformers et fastapi n'ont généralement pas besoin de build-essential sur python-slim.

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Pré-télécharger le modèle pour éviter de le télécharger à chaque démarrage du conteneur
RUN python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('paraphrase-multilingual-MiniLM-L12-v2')"

COPY . .

EXPOSE 8000

# Utilisation de uvicorn pour lancer l'app FastAPI
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
