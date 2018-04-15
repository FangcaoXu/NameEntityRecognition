# NameEntityRecognition

This repository contains codes to process the text information of the press releases scraped from state GOP party websites from 2013-2018. The scripts are written in Java and generate .json files with the following parts: 

•	ID: Index of Original Articles;

•	Title: Title of the news report;

•	Time: Date of the news report being released;

•	Content: The main body of the news presses;

•	Person: People detected in the news report;

•	Organization: Organizations detected in the news report;

•	Geolocation: Place names detected in the news report and associated coordinates;

The Stanford NLP is deployed here to extract all the name entities from the news presses. The scripts have two methods to geo-parse and return coordinatesthe of places names, one of which is Google Map API while another is the GeoNames API. It also has provided detection function for  people's and organizations' duplicated names as well as the hierarchy of place names (e.g. remove Pennsylvania from the result when the fine-grained geographic location, Philadelphia has been detected)
