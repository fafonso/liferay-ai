# Liferay DXP AI

This is the home of several AI independent initiatives and experiments with Liferay DXP.

The initial ideas and discussions that lead into this was on the Liferay Devcon 2017 context, and you can find our talk recording [here](https://www.youtube.com/watch?v=dEncGotvTZk)

This project is also a great example for several Liferay DXP features and extension points like:
* OSGi and Modular approach
* Integration with external systems
* Model Listeners
* System Settings (new Liferay DXP configuration approach)

## Features

* Image Recognition - Triggered to any D&M uploaded image
* Articles Recomendator - (Available soon)

## Prerequisite

The current image recognition module was built by leveraging on [Clarifai](https://www.clarifai.com/) services for image classification and as so, first thing to do is to create an account in clarifai and get your API Key.

## Installation
1. Clone this repository in your local environment
2. Build and deploy to your Liferay (DXP) installation

## Configuration
After deploying these modules, only thing that you need to do is to set your API Key. To achieve this, you just need to go to your portal's system settings, where you will find a new "Machine Learning" category. 

Please also see the other configuration settings and tune them to your likings.

## Team

Carlos Hernandez
Filipe Afonso

## Disclaimer
This portlet hasn't been thoroughly tested and is provided as is. You can freely develop it further to your own purposes but if you like it and have ideas you would like to see in this application I'd be glad to hear about those. Also many thanks in advance for any reported bug findings.