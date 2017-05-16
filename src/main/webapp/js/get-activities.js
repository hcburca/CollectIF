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
// serves to send an ajax request to get the activities
$(function () {
    var main = document.getElementById("main-activites");
    $.ajax({
        url: './ActionServletAdherent',
        type: 'POST',
        data: {
            action: 'listerActivites'
        },
        dataType: 'json',
        cache: false
    })
            .done(function (data) {
                if (data.result != null && data.result == "failure") {
                    var htmlServerProb = [
                        '<div class="alert alert-danger">',
                        '<p>' + data.message + '</p>',
                        '</div>'
                    ].join("\n");
                    $(main).append(htmlServerProb);
                } else {
                    //show activities 
                    var activites = data.activites;
                    if (activites.length === 0) {
                        var htmlNoActivities = [
                            '<div class="alert alert-danger">',
                            '<p>Aucune activité trouvée sur le serveur.</p>',
                            '</div>'
                        ].join("\n");
                        $(main).append(htmlNoActivities);
                    } else {
                        var i;
                        for (i = 0; i < activites.length; i++) {
                            var htmlActivity = [
                                '<div class="activite row row-flex row-margin">',
                                    '<div class="col-md-8">',
                                        '<h2>' + activites[i].denomination + '</h2>',
                                        '<p>' + activites[i].description + '</p>',
                                        '<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum in justo fermentum, cursus nulla in, suscipit velit. Nullam dui dui, vehicula eget faucibus eu, porttitor ac mi. Donec nisi arcu, fermentum id dictum id, elementum sed elit. Curabitur vel eros ex. Pellentesque quis commodo dolor. Ut faucibus, ipsum ac tincidunt ultrices, dolor tortor vestibulum arcu, eget venenatis felis lectus id sem. Ut nulla dolor, condimentum at mattis nec, tincidunt vel velit. Praesent at arcu vel dui luctus sollicitudin elementum et sem. Aenean mi tortor, commodo nec efficitur id, pulvinar eu sem. Vivamus non nibh ut tortor bibendum vulputate. Sed semper, quam et sollicitudin imperdiet, libero arcu scelerisque sapien, eget vestibulum diam lorem sed felis. Cras ultricies, arcu eget ultricies accumsan, mi lorem gravida tortor, ac gravida nibh enim vitae magna. Ut fermentum in lacus sit amet viverra. Suspendisse potenti. Vivamus ut tempus libero.</p>',
                                    '</div>',
                                    '<div class="col-md-4 img-activite">',
                                        '<img class="img-responsive img-rounded" title="' + activites[i].denomination + '" alt="' + activites[i].denomination + '" src="' + activites[i].imgUrl + '">',
                                        '<a class="btn btn-flex" href="demande.html?idActivite=' + activites[i].id + '">Je veux y participer <span class="glyphicon glyphicon-chevron-right"></span></a>',
                                    '</div>',
                                '</div>'         
                            ].join("\n");
                            $(main).append(htmlActivity);
                        }
                    }
                }
            })
            .fail(function () {
                var htmlFailure = [
                    '<div class="alert alert-danger">',
                    '<p>Un problème de serveur est survenu. Veuillez reéssayer plus tard.</p>',
                    '</div>'
                ].join("\n");
                $(main).append(htmlFailure);
            });
});
// -----

