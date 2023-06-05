FROM quay.io/opsmxpublic/ubifips:8.7
MAINTAINER sig-platform@spinnaker.io
COPY rosco-web/build/install/rosco /opt/rosco
COPY rosco-web/config              /opt/rosco
COPY halconfig/packer              /opt/rosco/config/packer

ENV KUSTOMIZE_VERSION=3.8.6

ENV PACKER_VERSION=1.8.1

WORKDIR /packer

RUN yum install -y java-17-openjdk-devel wget unzip curl tar git  openssl curl net-tools nettle && \
  wget https://releases.hashicorp.com/packer/${PACKER_VERSION}/packer_${PACKER_VERSION}_linux_amd64.zip && \
  unzip packer_${PACKER_VERSION}_linux_amd64.zip && \
  rm packer_${PACKER_VERSION}_linux_amd64.zip
  
RUN yum -y update

ENV PATH "/packer:$PATH"

# Install Helm 3
RUN wget https://raw.githubusercontent.com/kubernetes/helm/master/scripts/get-helm-3 && \
  chmod +x get-helm-3 && \
  ./get-helm-3 && \
  rm get-helm-3 && \
  mv /usr/local/bin/helm /usr/local/bin/helm3

# Install Helm 2
#RUN wget https://raw.githubusercontent.com/kubernetes/helm/master/scripts/get && chmod +x get && ./get &&  rm get

RUN mkdir kustomize && \
  curl -s -L https://github.com/kubernetes-sigs/kustomize/releases/download/kustomize/v${KUSTOMIZE_VERSION}/kustomize_v${KUSTOMIZE_VERSION}_linux_amd64.tar.gz|\
  tar xvz -C kustomize/

ENV PATH "kustomize:$PATH"

RUN useradd spinnaker
RUN mkdir -p /opt/rosco/plugins
USER spinnaker
CMD ["/opt/rosco/bin/rosco"]
