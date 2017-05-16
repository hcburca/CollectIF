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

var moments = ["matin", "après-midi", "soir"];

//sends an ajax request to get the demands and archive events for the user
$(function () {
    $.ajax({
        url: './ActionServletAdherent',
        type: 'POST',
        dataType: 'json',
        data: {
            action: 'listerHistorique'
        }
    })
            .done(function (data) {
                if (data.length === 0) {
                    var htmlEmpty = [
                        '<div class="alert alert-warning">',
                        '<p>Votre archive est <strong>vide</strong>.</p>',
                        '</div>',
                        '<hr class="hr-70">'
                    ].join("\n");
                    $("#main-historique h1").after(htmlEmpty);
                } else {
                    orderDemands(data);
                    var htmlEvents = [];
                    for (var i = data.length - 1; i >= 0; i--) {
                        var htmlEvent = [
                            '<div class="row row-flex">',
                            '<div class="col-md-3">',
                            '<img class="img-responsive img-rounded" title="' + data[i].denomination + '" alt="' + data[i].denomination + '" src="' + data[i].imgUrl + '">',
                            '</div>',
                            '<div class="col-md-9">',
                            '<div class="row-flex">',
                            '<div class="col-md-2">',
                            '<p><strong>Activité</strong></p>',
                            '</div>',
                            '<div class="col-md-10">',
                            '<p>' + data[i].denomination + '</p>',
                            '</div>',
                            '</div>',
                            '<div class="row-flex">',
                            '<div class="col-md-2">',
                            '<p><strong>Date</strong></p>',
                            '</div>',
                            '<div class="col-md-10">',
                            '<p>' + data[i].date + ' (' + moments[data[i].momentOrder] + ')</p>',
                            '</div>',
                            '</div>',
                            '<div class="row-flex">',
                            '<div class="col-md-2">',
                            '<p><strong>Status</strong></p>',
                            '</div>',
                            '<div class="col-md-10">',
                            '<p>' + data[i].status + '</p>',
                            '</div>',
                            '</div>'
                        ];
                        //adds the participants, if any
                        if (data[i].participants != null) {
                            var participants = data[i].participants;
                            var participantsArray = [];
                            for (var j = 0; j < participants.length; j++) {
                                participantsArray.push(participants[j].name);
                            }
                            var htmlParticipants = [
                                '<div class="row-flex">',
                                '<div class="col-md-2">',
                                '<p><strong>Participants</strong></p>',
                                '</div>',
                                '<div class="col-md-10">',
                                '<p>' + participantsArray.join(", ") + '</p>',
                                '</div>',
                                '</div>',
                            ];
                            htmlEvent = htmlEvent.concat(htmlParticipants);
                        }
                        htmlEvent = htmlEvent.concat([
                            '</div>',
                            '</div>',
                            '<hr class="hr-70">'
                        ]);
                        htmlEvents = htmlEvents.concat(htmlEvent);
                    }
                    $("#main-historique h1").after(htmlEvents.join("\n"));
                }
            })
            .fail(function () {
                var htmlEmpty = [
                    '<div class="alert alert-danger">',
                    '<p>Une erreur s\'est produite sur le serveur. Veuillez réessayer plus tard.</p>',
                    '</div>',
                    '<hr class="hr-70">'
                ].join("\n");
                $("#main-historique h1").after(htmlEmpty);
            });
});

