FROM debian:bookworm-slim

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        build-essential \
        libc6 \
        libgcc-s1 \
        libgssapi-krb5-2 \
        libicu72 \
        libssl3 \
        libstdc++6 \
        zlib1g \
        iputils-ping \
        iproute2 \
        sudo && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Virtuelle Umgebung einrichten
ENV VIRTUAL_ENV=/opt/venv
ENV PATH="$VIRTUAL_ENV/bin:$PATH"

# Benutzer 'server' hinzufügen und Verzeichnisse erstellen
RUN useradd -m server && \
    mkdir -p /home/server/tmp /home/server/collector /home/server/encapsulation && \
    chown -R server:server /home/server

# Skript und Anwendungen kopieren und Berechtigungen setzen
COPY --chown=server:server --chmod=755 entrypoint.sh /usr/local/bin/entrypoint.sh
COPY --chown=server:server collector /home/server/collector
COPY --chown=server:server encapsulation /home/server/encapsulation

# Arbeitsverzeichnis und Benutzer setzen
USER server
WORKDIR /home/server

# Temporäres Verzeichnis definieren
ENV TEMP=/home/server/tmp

# Port freigeben
EXPOSE 8080

# Entrypoint und Standardbefehl festlegen
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
CMD ["serveAndCollect"]
