/* 
 * Copyright (C) 2017 Horia
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
var events;                     //holds the json events 
var places;                     //holds the json places
var slideIndex;                 //stores the index of the current event (from 1 to n)
var map;                        //the gmap
var markersParticipants = [];   //an array for the map makers of the currently selected participants
var markersPlaces = [];         //an array for the map makers of the currently selected places

//shows the respective participants for the current event
function changeEvent() {
    //removing markers from map
    removeMarkersFromMap(markersParticipants);
    removeMarkersFromMap(markersPlaces);
    markersParticipants = [];
    markersPlaces = [];
    displayMarkers();
    //removing place and participant selection
    $("#participants__select option:selected").prop("selected", false);
    $("#lieu__select option:selected").prop("selected", false);
    //changing places
    $('#filtre').val(events[slideIndex - 1].placeType);
    showPlaces();
    //changing participants
    $('#participants__select option').each(function (j, participant) {
        if (participant.value == slideIndex) {
            participant.hidden = false;
        } else {
            participant.hidden = true;
        }
    });
    //reseting tout-afficher
    $('input[name="tout-afficher"]').bootstrapSwitch('state', false, true);
    //reseting the distances for the participants
    resetDistances();
    //reseting paf
    $('#paf-input').val('');
    $('#paf-per-person').text('0');
    //hiding paf for free events
    if (events[slideIndex - 1].payed == "Cette activité est gratuite.") {
        $('#footer__paf').hide();
        $('#event-status').addClass('side-margins-auto');
    } else {
        $('#footer__paf').show();
        $('#event-status').removeClass('side-margins-auto')
    }
    //updating the page status
    updatePage();
}

//resets the distances for the participants and the total distance
function resetDistances() {
    var participants = $('#participants__select option');
    $.each(participants, function (i, part) {
        $(part.firstElementChild).remove();
    });
    //reseting total distance 
    $('#total-distance').text('0');
}

//shows the respective places depending on the current place filter 
function showPlaces() {
    var placeFilter = $("#filtre").val();
    $('#lieu__select option').each(function (j, place) {
        if (place.value === placeFilter) {
            place.hidden = false;
        } else {
            place.hidden = true;
        }
    });
}

//calcultes the PAF per person and displays it
function calculatePaf() {
    var pafInput = document.getElementById('paf-input');
    if (pafInput.value == "") {
        $(pafInput).val(0);
    } else {
        $(pafInput).val(parseFloat(pafInput.value));
    }
    $('#paf-per-person').text((pafInput.value / events[slideIndex - 1].participants.length).toPrecision(3));
}

//calculates the distances between the current place and each participant
function calculateDistances() {
    var place = places[$('#lieu__select option:visible:selected').attr('tabindex')];
    var coordPlace = new google.maps.LatLng(place.lat, place.long);
    var participantsJson = events[slideIndex - 1].participants;
    var participants = $('#participants__select option:visible');
    var sumDist = 0;
    $.each(participants, function (i, participant) {
        var coordPart = new google.maps.LatLng(participantsJson[participant.tabIndex].lat, participantsJson[participant.tabIndex].long);
        var distance = Math.floor(google.maps.geometry.spherical.computeDistanceBetween(coordPlace, coordPart));
        //showing the distance next to the name of the participant
        var distSpan = document.createElement('span');
        distSpan.innerHTML = " (" + distance + " m)";
        $(participant.firstElementChild).remove();
        participant.appendChild(distSpan);

        sumDist += distance;
    });
    //showing the total distance
    $('#total-distance').text(sumDist);
}

//adds a Google map to the page
//used for the callback of the google maps script inclusion
function addMap() {
    var mapOptions = {
        center: new google.maps.LatLng(45.7640, 4.8357),
        zoom: 10,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    }
    map = new google.maps.Map(document.getElementById("map"), mapOptions);
    //adding event listener for zoom to stop over-zooming
    //if the zoom is set to lower than 13 than it blocks at 13
    google.maps.event.addListener(map, 'zoom_changed', function () {
        zoomChangeBoundsListener =
                google.maps.event.addListener(map, 'bounds_changed', function (event) {
                    if (this.getZoom() > 13 && this.initialZoom == true) {
                        // Change max/min zoom here
                        this.setZoom(13);
                        this.initialZoom = false;
                    }
                    google.maps.event.removeListener(zoomChangeBoundsListener);
                });
    });
}

//sets the map to null for all markers in the array
function removeMarkersFromMap(markers) {
    for (var i = 0; i < markers.length; i++) {
        markers[i].setMap(null);
    }
}

//displays the current participant markers and places markers
function displayMarkers() {
    //if no markers than map is centered on Lyon
    if (markersParticipants.length === 0 && markersPlaces.length === 0) {
        map.setCenter(new google.maps.LatLng(45.7640, 4.8357));
        map.setZoom(10);
    } else {
        //else add the markers to a LatLngBounds object and then
        //center map on the center of the bounds 
        var i;
        var bounds = new google.maps.LatLngBounds();
        for (i = 0; i < markersParticipants.length; i++) {
            markersParticipants[i].setMap(map);
            bounds.extend(markersParticipants[i].getPosition());
        }
        for (i = 0; i < markersPlaces.length; i++) {
            markersPlaces[i].setMap(map);
            bounds.extend(markersPlaces[i].getPosition());
        }
        map.initialZoom = true;
        map.fitBounds(bounds);
    }
}

//updates the different elements of the page depending on how the event has advanced
function updatePage() {
    var status = document.getElementById('event-status');
    var validerButton = document.getElementById('event-submit');
    var placeSelection = $('#lieu__select option:visible:selected');
    var pricedEvent = (events[slideIndex - 1].payed !== "Cette activité est gratuite.");
    var paf = $('#paf-input').val();
    if (placeSelection.length === 0 && pricedEvent && paf === "") {
        validerButton.disabled = true;
        $('#event-status').removeClass("alert alert-success").addClass("alert alert-warning");
        status.innerHTML = 'Veuillez choisir un lieu pour l\'evenement et specifier la PAF.';
    } else if (pricedEvent && paf === "") {
        validerButton.disabled = true;
        $('#event-status').removeClass("alert alert-success").addClass("alert alert-warning");
        status.innerHTML = 'Veuillez specifier la PAF.';
    } else if (placeSelection.length === 0) {
        validerButton.disabled = true;
        $('#event-status').removeClass("alert alert-success").addClass("alert alert-warning");
        status.innerHTML = 'Veuillez choisir un lieu pour l\'evenement.';
    } else {
        validerButton.disabled = false;
        $('#event-status').removeClass("alert alert-warning").addClass("alert alert-success");
        if (pricedEvent) {
            status.innerHTML = "Vous pouvez maintenent valider l'evenement: " + events[slideIndex - 1].denomination + ", à l'adresse: " + $(placeSelection).text() + ", avec une PAF de: " + paf + " euro.";
        } else {
            status.innerHTML = "Vous pouvez maintenent valider l'evenement: " + events[slideIndex - 1].denomination + ", à l'adresse: " + $(placeSelection).text() + ".";
        }
    }
}

//sends an ajax request to validate the place and paf for the event
function sendRequestValidateEvent() {
    var placeSelection = $('#lieu__select option:visible:selected').attr('tabindex');
    var pricedEvent = (events[slideIndex - 1].payed !== "Cette activité est gratuite.");
    var paf = $('#paf-input').val();
    var dataToSend = {
        action: 'validerEvenement',
        idEvent: events[slideIndex - 1].id,
        idPlace: places[placeSelection].id
    };
    if (pricedEvent) {
        dataToSend['paf'] = paf;
    }
    $.ajax({
        beforeSend: function () {
            document.getElementById("event-submit").disabled = true;
            //document.getElementById('event-cancel').disabled = true;
        },
        url: './ActionServletAdmin',
        type: 'POST',
        data: dataToSend,
        dataType: 'json'
    })
            .done(function (data) {
                var status = document.getElementById('event-status');
                if (data.result == "success") {
                    //appends a modal popup to the body and shows it
                    var htmlPopup = [
                        '<div id="modal-success" class="modal fade">',
                        '<div class="modal-dialog" role="document">',
                        '<div class="modal-content">',
                        '<div class="modal-header">',
                        '<h5 class="modal-title">Succès!</h5>',
                        '<button type="button" class="close" data-dismiss="modal" aria-label="Close">',
                        '<span aria-hidden="true">&times;</span>',
                        '</button>',
                        '</div>',
                        '<div class="modal-body">',
                        '<div class="alert alert-success">',
                        '<p>' + data.message + '</p>',
                        '</div>',
                        '</div>',
                        '<div class="modal-footer">',
                        '<button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>',
                        '</div>',
                        '</div>',
                        '</div>',
                        '</div>'
                    ].join("\n");
                    $('body').append(htmlPopup);
                    $('#modal-success').modal({show: false});
                    $('#modal-success').modal('show');
                    //adding an event listener to the modal to redirect the page to the url indicated by the 
                    //server on closure of the modal
                    $('#modal-success').on('hidden.bs.modal', function () {
                        $(location).attr('href', data.redirect);
                    });
                } else if (data.result == "failure") {
                    $('#event-status').removeClass("alert alert-warning alert-success").addClass("alert alert-danger");
                    status.innerHTML = data.message;
                } else {
                    $('#event-status').removeClass("alert alert-warning alert-success").addClass("alert alert-danger");
                    status.innerHTML = "Problem inconnue, veuillez réessayer plus tard.";
                }
            })
            .fail(function () {
                $('#event-status').removeClass("alert alert-warning alert-success").addClass("alert alert-danger");
                $('#event-status').text("Problem inconnue, veuillez réessayer plus tard.");
            })
            .always(function () {
                document.getElementById("event-submit").disabled = false;
            })
}

//checks the pages of the demands carousel to hide or show navigation buttons
//depeding on the number of slides and the current slide
var checkitem = function () {
    var $this;
    $this = $("#eventCarousel");
    if ($("#eventCarousel .carousel-inner .item").length === 1) {
        $this.find("#control_next").addClass("disabled");
        $this.find("#control_prev").addClass("disabled");
    } else if ($("#eventCarousel .carousel-inner .item:first").hasClass("active")) {
        $this.find("#control_prev").addClass("disabled");
        $this.find("#control_next").removeClass("disabled");
    } else if ($("#eventCarousel .carousel-inner .item:last").hasClass("active")) {
        $this.find("#control_next").addClass("disabled");
        $this.find("#control_prev").removeClass("disabled");
    } else {
        $this.find("#control_next").removeClass("disabled");
        $this.find("#control_prev").removeClass("disabled");
    }
};

//sends ajax request to server to get the list of events that are waiting to be attributed a place
$(function () {
    $.ajax({
        url: './ActionServletAdmin',
        type: 'POST',
        dataType: 'json',
        cached: false,
        data: {
            action: 'listerEvenementsAttente'
        }
    })
            .done(function (data) {
                if (data.result != null && data.result == "failure") {
                    var htmlFailure = [
                        '<div class="item active">',
                        '<h4 class="alert alert-danger side-margins-auto">' + data.message + '</h4>',
                        '</div>'
                    ].join("\n");
                    $("#eventCarousel-inner").append(htmlFailure);
                    //disable buttons
                    $('input[name="tout-afficher"]').bootstrapSwitch('toggleDisabled', true);
                    $('#paf-input').attr('disabled', true);
                    $('#event-submit').attr('disabled', true);
                } else {
                    events = data.events;
                    places = data.places;
                    if (events.length === 0) {
                        var htmlNoEvents = [
                            '<div class="item active">',
                            '<h4 class="alert alert-info side-margins-auto">Aucun evenement en attente d\'attribution de lieu.</h4>',
                            '</div>'
                        ].join("\n");
                        $("#eventCarousel-inner").append(htmlNoEvents);
                        //disable buttons
                        $('input[name="tout-afficher"]').bootstrapSwitch('toggleDisabled', true);
                        $('#paf-input').attr('disabled', true);
                        $('#event-submit').attr('disabled', true);
                    } else {
                        //add the number of events to the navigation buttons
                        $("#nr-events").text("1/" + events.length);
                        //add the types of places
                        var lookup = {};
                        var items = places;
                        var distinctPlaces = [];
                        for (var item, i = 0; item = items[i++]; ) {
                            var type = item.type;
                            if (!(type in lookup)) {
                                lookup[type] = 1;
                                distinctPlaces.push(type);
                            }
                        }
                        $.each(distinctPlaces, function (i, place) {
                            $('#filtre').append($('<option>', {
                                value: place,
                                text: place
                            }));
                        });
                        //add the places
                        $.each(places, function (i, place) {
                            $(document).find('#lieu__select').append($('<option>', {
                                value: place.type,
                                tabindex: i,
                                text: place.address
                            }));
                        });
                        //add the events
                        for (var i = 0; i < events.length; i++) {
                            var itemClasses = "item";
                            if (i === 0) {
                                itemClasses += " active";
                            }
                            var j = 0; //for participant tabIndex
                            var htmlEvent = [
                                '<div class="' + itemClasses + '">',
                                '<img id="event__img img-responsive img-rounded" title="' + events[i].denomination + '" alt="' + events[i].denomination + '" src="' + events[i].imgUrl + '">',
                                '<div class="two-paragraphs" id="tp-left">',
                                '<h3 id="event__title"><em>' + events[i].denomination + '</em></h3>',
                                '<p id="event__payant"><i class="fa fa-info-circle"></i> ' + events[i].payed + '</p>',
                                '</div>',
                                '<div class="two-paragraphs" id="tp-right">',
                                '<h4 id="event__date">' + events[i].date + ', ' + events[i].moment + '</h4>',
                                '<p class="event__current-place"></p>',
                                '</div>',
                                '</div>'
                            ].join("\n");
                            $("#eventCarousel-inner").append(htmlEvent);
                            //participants
                            $.each(events[i].participants, function (j, participant) {
                                $('#participants__select').append($('<option>', {
                                    value: i + 1,
                                    tabindex: j++,
                                    text: participant.name
                                }));
                            });
                        }
                        slideIndex = 1;
                        changeEvent();
                        updatePage();
                    }
                }
            })
            .fail(function () {
                var htmlFailure = [
                    '<div class="item active">',
                    '<h4 class="alert alert-danger side-margins-auto">Problem de serveur, veuillez réessayer plus tard.</h4>',
                    '</div>'
                ].join("\n");
                $("#eventCarousel-inner").append(htmlFailure);
                //disable buttons
                $('input[name="tout-afficher"]').bootstrapSwitch('toggleDisabled', true);
                $('#paf-input').attr('disabled', true);
                $('#event-submit').attr('disabled', true);
            })
            .always(function () {
                checkitem();
            })
});

//adds various event listeners
//initializes various modules
$(document).ready(function () {
    //showing the switches
    $("[name='tout-afficher']").bootstrapSwitch();
    //adding switch event to the bootstrap switch for the participants
    $('#part__myonoffswitch').on('switchChange.bootstrapSwitch', function (event, state) {
        if (state) {
            //if switched on, display all participants on the map
            var participants = events[slideIndex - 1].participants;
            var allParticipants = $('#participants__select option:visible');
            $.each(allParticipants, function (i, part) {
                var participant = participants[$(part).attr('tabindex')];
                var pos = new google.maps.LatLng(participant.lat, participant.long);
                var marker = new google.maps.Marker({
                    position: pos,
                    map: null,
                    title: participant.name,
                    icon: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png'
                });
                var infowindow = new google.maps.InfoWindow({
                    content: participant.name
                });
                marker.addListener('click', function () {
                    infowindow.open(map, marker);
                });
                markersParticipants.push(marker);
            });
            displayMarkers();
        } else {
            //if switched off, remove the markers of the participants from the map
            removeMarkersFromMap(markersParticipants);
            markersParticipants = [];
            if ($('#participants__select option:visible:selected').length === 0) {
                displayMarkers();
            } else {
                $('#participants__select').trigger('change');
            }
        }
    });
    //adding switch event to the bootstrap switch for the places
    $('#lieu__myonoffswitch').on('switchChange.bootstrapSwitch', function (event, state) {
        if (state) {
            //if switched on, display all places on the map
            var allPlaces = $('#lieu__select option:visible');
            $.each(allPlaces, function (i, place) {
                var placeJson = places[$(place).attr('tabindex')];
                var pos = new google.maps.LatLng(placeJson.lat, placeJson.long);
                var marker = new google.maps.Marker({
                    position: pos,
                    map: null,
                    title: placeJson.address,
                    icon: 'http://maps.google.com/mapfiles/ms/icons/red-dot.png'
                });
                var htmlContent = [
                    '<p>' + placeJson.name + '</p>',
                    '<p>' + placeJson.address + '</p>'
                ].join("\n");
                var infowindow = new google.maps.InfoWindow({
                    content: htmlContent
                });
                marker.addListener('click', function () {
                    infowindow.open(map, marker);
                });
                markersPlaces.push(marker);
            });
            displayMarkers();
        } else {
            //if switched off, remove the markers of the places from the map
            removeMarkersFromMap(markersPlaces);
            markersPlaces = [];
            if ($('#lieu__select option:visible:selected').length === 0) {
                displayMarkers();
            } else {
                $('#lieu__select').trigger('change');
            }
        }
    });
    //adding change event to the filter for the places
    $("#filtre").on('change', function () {
        showPlaces();
        //remove the place displayed under the date
        $('#eventCarousel .active .event__current-place').text("");
        //reset the switch for the places and the markers of the places on the map
        $("#lieu__select option:selected").prop("selected", false);
        $('#lieu__myonoffswitch').trigger('switchChange.bootstrapSwitch', false);
        //$('#lieu__myonoffswitch').bootstrapSwitch('state', false, false);
        //reseting the distances
        resetDistances();
        updatePage();
    });

    //adding click events to the next and prev buttons of the carousel
    $('#control_next').on('click', function () {
        $('#eventCarousel').carousel("next");
        updatePage();
    });
    $('#control_prev').on('click', function () {
        $('#eventCarousel').carousel("prev");
        updatePage();
    });

    //adding change event to the place selection
    $('#lieu__select').on('change', function () {
        //change the place that is displayed under the date
        $('#eventCarousel .active .event__current-place').text($("#lieu__select option:selected").text());
        if (!$('#lieu__myonoffswitch').is(":checked")) {
            removeMarkersFromMap(markersPlaces);
            markersPlaces = [];
            var place = places[$('#lieu__select option:visible:selected').attr('tabindex')];
            var pos = new google.maps.LatLng(place.lat, place.long);
            var marker = new google.maps.Marker({
                position: pos,
                map: null,
                title: place.address,
                icon: 'http://maps.google.com/mapfiles/ms/icons/red-dot.png'
            });
            var htmlContent = [
                '<p>' + place.name + '</p>',
                '<p>' + place.address + '</p>'
            ].join("\n");
            var infowindow = new google.maps.InfoWindow({
                content: htmlContent
            });
            marker.addListener('click', function () {
                infowindow.open(map, marker);
            });
            markersPlaces.push(marker);
            displayMarkers();
        }
        updatePage();
        calculateDistances();
    });

    //adding change event listener to the participants selection
    $('#participants__select').on('change', function () {
        if (!$('#part__myonoffswitch').is(":checked")) {
            removeMarkersFromMap(markersParticipants);
            markersParticipants = [];
            var participants = events[slideIndex - 1].participants;
            var participant = participants[$('#participants__select option:visible:selected').attr('tabindex')];
            var pos = new google.maps.LatLng(participant.lat, participant.long);
            var marker = new google.maps.Marker({
                position: pos,
                map: null,
                title: participant.name,
                icon: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png'
            });
            var infowindow = new google.maps.InfoWindow({
                content: participant.name
            });
            marker.addListener('click', function () {
                infowindow.open(map, marker);
            });
            markersParticipants.push(marker);
            displayMarkers();
        }
    });

    //launching focusout event when ENTER is pressed 
    $('#paf-input').on('keypress', function (e) {
        if (e.which === 13) {
            $(this).blur();
        }
    });
    //preventing minus from being typed
    $('#paf-input').on('keydown', function (e) {
        if (e.which === 189) {
            e.preventDefault();
        }
    });
    //adds input listener to check that a positive number is inserted
    $('#paf-input').on('blur', function () {
        calculatePaf();
        updatePage();
    });

    //adds click listener to validate button
    $('#event-submit').on('click', sendRequestValidateEvent);

    //check navigation button on event change
    $("#eventCarousel").on("slid.bs.carousel", "", function (event) {
        checkitem();
        if (event.direction == "left") {
            slideIndex++;
        } else if (event.direction == "right") {
            slideIndex--;
        }
        changeEvent();
        $("#nr-events").text(slideIndex + "/" + events.length);
    });
});




