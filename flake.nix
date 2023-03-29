{
  description = "demo of ngrok agent library in Java";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixpkgs-unstable";

    flake-utils = {
      url = "github:numtide/flake-utils";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
        };
        java-toolchain = with pkgs; [
          openjdk17_headless
          maven
        ];
      in
      {
        devShell = pkgs.mkShell {
          buildInputs = with pkgs; [
            java-toolchain
          ];
        };
      });
}
