vagrant_root = File.dirname(__FILE__)
vagrant_folder_name = File.basename(vagrant_root)

Vagrant.require_version ">= 1.6.3"

def parse_ignore_file(file)
  IO.read(file)
    .split("\n")
    .reject{|line| line.empty? || line.start_with?("#")}
end

Vagrant.configure("2") do |config|
  config.vm.define "boot2docker"

  config.vm.box = "blinkreaction/boot2docker"
  config.vm.box_version = "1.6.0"
  config.vm.box_check_update = false

  config.vm.synced_folder vagrant_root, vagrant_root,
    type: "rsync",
    rsync__exclude: parse_ignore_file(".gitignore").uniq,
    rsync__args: ["--verbose", "--archive", "--delete", "-z", "--chmod=ugo=rwX"]

  config.vm.provider "virtualbox" do |v|
    v.gui = false
    v.name = vagrant_folder_name + "_boot2docker"
    v.cpus = 1
    v.memory = 2048
  end

  # Allow Mac OS X docker client to connect to Docker without TLS auth.
  # https://github.com/deis/deis/issues/2230#issuecomment-72701992
  config.vm.provision "shell" do |s|
    s.inline = <<-SCRIPT
      echo 'DOCKER_TLS=no' >> /var/lib/boot2docker/profile
      /etc/init.d/docker restart
    SCRIPT
  end  
end