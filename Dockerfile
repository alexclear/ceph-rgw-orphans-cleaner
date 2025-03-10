FROM quay.io/ceph/ceph:v18.2.2

RUN sed -i 's/mirrorlist/#mirrorlist/g' /etc/yum.repos.d/CentOS-* && \
    sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-* && \
    rm -f /etc/yum.repos.d/_copr:copr.fedorainfracloud.org:tchaikov:python-scikit-learn.repo && \
    rm -f /etc/yum.repos.d/ceph.repo && \
    cat <<EOF > /etc/yum.repos.d/ceph.repo
[Ceph]
name=Ceph packages
baseurl=https://download.ceph.com/rpm-18.2.0/el8/x86_64/
enabled=1
gpgcheck=1
gpgkey=https://download.ceph.com/keys/release.asc
EOF
RUN yum install -y python3 python3-pip

COPY delete-orphans.sh /tmp/
CMD ["sh", "-c", "/tmp/delete-orphans.sh"]
